package xyz.quartzframework.data.query;

import lombok.val;
import xyz.quartzframework.data.entity.Attribute;
import xyz.quartzframework.data.storage.StorageDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodQueryParser implements QueryParser {

    @Override
    public boolean supports(Method method) {
        val a = method.getAnnotation(Query.class);
        return a == null && isInPattern(method);
    }

    private boolean isInPattern(Method method) {
        return method.getName().matches("^(find|count|exists).*");
    }

    @Override
    public String queryString(Method method) {
        return method.getName();
    }

    private static final Map<String, Operation> suffixAlias = Map.ofEntries(
            Map.entry("After", Operation.GREATER_THAN),
            Map.entry("Before", Operation.LESS_THAN),
            Map.entry("GreaterThan", Operation.GREATER_THAN),
            Map.entry("LessThan", Operation.LESS_THAN),
            Map.entry("GreaterThanOrEqual", Operation.GREATER_THAN_OR_EQUAL),
            Map.entry("LessThanOrEqual", Operation.LESS_THAN_OR_EQUAL),
            Map.entry("Not", Operation.NOT_EQUAL),
            Map.entry("Like", Operation.LIKE),
            Map.entry("NotLike", Operation.NOT_LIKE),
            Map.entry("In", Operation.IN),
            Map.entry("NotIn", Operation.NOT_IN),
            Map.entry("IsNull", Operation.IS_NULL),
            Map.entry("IsNotNull", Operation.IS_NOT_NULL),
            Map.entry("True", Operation.EQUAL),
            Map.entry("False", Operation.EQUAL)
    );

    @Override
    public DynamicQueryDefinition parse(Method method, StorageDefinition storageDefinition) {
        val name = queryString(method);
        QueryAction action = extractAction(name);
        String stripped = stripPrefix(name, action);

        boolean distinct = false;
        if (stripped.startsWith("Distinct")) {
            distinct = true;
            stripped = stripped.substring("Distinct".length());
        }

        List<QueryCondition> queryConditions = new ArrayList<>();
        List<QuerySubstitution> substitutions = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        Integer limit = null;
        String conditionPart = stripped;

        if (stripped.contains("OrderBy")) {
            String[] split = stripped.split("OrderBy", 2);
            conditionPart = split[0];
            orders = parseOrderPart(split[1], storageDefinition);
        }

        if (conditionPart.startsWith("Top")) {
            Matcher m = Pattern.compile("Top(\\d+)(.*)").matcher(conditionPart);
            if (m.matches()) {
                limit = Integer.parseInt(m.group(1));
                conditionPart = m.group(2);
            }
        } else if (conditionPart.startsWith("First")) {
            limit = 1;
            conditionPart = conditionPart.substring(5);
        }

        if (conditionPart.startsWith("By")) {
            conditionPart = conditionPart.substring(2);
        }

        if (!conditionPart.isEmpty()) {
            parseConditions(conditionPart, storageDefinition, queryConditions, substitutions);
        }

        return new DynamicQueryDefinition(
                method,
                action,
                substitutions,
                queryConditions,
                orders,
                limit,
                distinct,
                false,
                null,
                storageDefinition.entityClass(),
                null
        );
    }

    private QueryAction extractAction(String methodName) {
        if (methodName.startsWith("find")) return QueryAction.FIND;
        if (methodName.startsWith("count")) return QueryAction.COUNT;
        if (methodName.startsWith("exists")) return QueryAction.EXISTS;
        throw new IllegalArgumentException("Unknown query action: " + methodName);
    }

    private String stripPrefix(String methodName, QueryAction action) {
        return methodName.substring(action.name().length()).replaceFirst("^By", "By");
    }

    private void parseConditions(String part, StorageDefinition storageDefinition, List<QueryCondition> outConditions, List<QuerySubstitution> outSubs) {
        String[] orBlocks = part.split("Or");
        int paramIndex = 0;

        for (int i = 0; i < orBlocks.length; i++) {
            String orBlock = orBlocks[i];
            String[] andTokens = orBlock.split("And");

            for (String token : andTokens) {
                QueryCondition condition = parseConditionToken(token, paramIndex, storageDefinition, outSubs);
                if (i > 0) condition.setOr(true);
                outConditions.add(condition);
                paramIndex++;
            }
        }
    }

    private QueryCondition parseConditionToken(String token, int index, StorageDefinition storageDefinition, List<QuerySubstitution> outSubs) {
        boolean ignoreCase = false;
        CaseFunction caseFunction = CaseFunction.NONE;
        String rawProperty = token;

        if (token.endsWith("IgnoreCase")) {
            token = token.substring(0, token.length() - "IgnoreCase".length());
            ignoreCase = true;
            caseFunction = CaseFunction.LOWER;
        }

        for (String suffix : suffixAlias.keySet().stream().sorted(Comparator.comparingInt(String::length).reversed()).toList()) {
            if (token.endsWith(suffix)) {
                Operation op = suffixAlias.get(suffix);
                String prop = token.substring(0, token.length() - suffix.length());
                String fieldPath = toNestedFieldPath(prop, storageDefinition.entityClass());
                switch (suffix) {
                    case "True" -> outSubs.add(QuerySubstitution.literal(true, "true"));
                    case "False" -> outSubs.add(QuerySubstitution.literal(false, "false"));
                    case "IsNull", "IsNotNull" -> outSubs.add(QuerySubstitution.literal(null, suffix.toLowerCase()));
                    default -> outSubs.add(QuerySubstitution.positional(String.valueOf(index), "?" + (index)));
                }

                return new QueryCondition(
                        token,
                        new AttributePath(rawProperty, fieldPath, caseFunction),
                        op,
                        suffix,
                        ignoreCase
                );
            }
        }
        String field = toNestedFieldPath(token, storageDefinition.entityClass());
        outSubs.add(QuerySubstitution.positional(String.valueOf(index), "?" + (index + 1)));

        return new QueryCondition(
                token,
                new AttributePath(rawProperty, field, caseFunction),
                Operation.EQUAL,
                "?" + (index + 1),
                ignoreCase
        );
    }

    private List<Order> parseOrderPart(String orderPart, StorageDefinition storageDefinition) {
        List<Order> orders = new ArrayList<>();
        Pattern pattern = Pattern.compile("([A-Z][a-zA-Z0-9]*)(Asc|Desc)$");
        Matcher matcher = pattern.matcher(orderPart);
        while (matcher.find()) {
            String prop = matcher.group(1);
            String direction = matcher.group(2);
            orders.add(new Order(toNestedFieldPath(prop, storageDefinition.entityClass()), "Desc".equalsIgnoreCase(direction)));
        }
        return orders;
    }

    private String toNestedFieldPath(String token, Class<?> rootClass) {
        StringBuilder resolvedPath = new StringBuilder();
        Class<?> current = rootClass;

        Matcher matcher = Pattern.compile("[A-Z][a-z0-9]*").matcher(token);
        while (matcher.find()) {
            String segment = matcher.group();
            String fieldMatch = null;
            Field matchedField = null;
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase(segment)) {
                    fieldMatch = field.getName();
                    matchedField = field;
                    break;
                }
                Attribute attr = field.getAnnotation(Attribute.class);
                if (attr != null && attr.value().equalsIgnoreCase(segment)) {
                    fieldMatch = field.getName();
                    matchedField = field;
                    break;
                }
            }
            if (fieldMatch == null) {
                return lowerFirst(token);
            }
            if (!resolvedPath.isEmpty()) {
                resolvedPath.append(".");
            }
            resolvedPath.append(fieldMatch);

            current = matchedField.getType();
        }

        return resolvedPath.toString();
    }

    private String lowerFirst(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}