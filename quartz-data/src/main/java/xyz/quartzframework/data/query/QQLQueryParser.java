package xyz.quartzframework.data.query;

import lombok.val;
import xyz.quartzframework.data.storage.StorageDefinition;
import xyz.quartzframework.data.util.ParameterBindingUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QQLQueryParser implements QueryParser {

    @Override
    public boolean supports(Method method) {
        val a = method.getAnnotation(Query.class);
        return a != null && isInPattern(a);
    }

    private boolean isInPattern(Query query) {
        String sanitized = query.value().replaceAll("\\s+", " ").trim();
        return sanitized.matches("^(find|count|exists).*");
    }

    @Override
    public String queryString(Method method) {
        val annotation = method.getAnnotation(Query.class);
        return annotation != null ? annotation.value() : null;
    }

    private String sanitizeQuery(String query) {
        return query.replaceAll("[\\s\\r\\n]+", " ").trim();
    }

    @Override
    public DynamicQueryDefinition parse(Method method, StorageDefinition storageDefinition) {
        val rawQuery = sanitizeQuery(queryString(method));
        val lower = rawQuery.toLowerCase(Locale.ROOT).trim();

        QueryAction action;
        if (lower.startsWith("find")) action = QueryAction.FIND;
        else if (lower.startsWith("count")) action = QueryAction.COUNT;
        else if (lower.startsWith("exists")) action = QueryAction.EXISTS;
        else throw new IllegalArgumentException("Unknown query action: " + rawQuery);

        String query = rawQuery.substring(action.name().length()).trim();
        boolean distinct = false;
        Integer limit = null;

        if (query.toLowerCase().startsWith("distinct")) {
            distinct = true;
            query = query.substring("distinct".length()).trim();
        }

        Matcher topMatch = Pattern.compile("top\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(query);
        if (topMatch.find()) {
            limit = Integer.parseInt(topMatch.group(1));
            query = query.replaceFirst("(?i)top\\s+\\d+", "").trim();
        }

        if (query.toLowerCase().startsWith("by ")) {
            query = query.substring(3).trim();
        }

        List<QueryCondition> queryConditions = new ArrayList<>();
        List<QuerySubstitution> substitutions = new ArrayList<>();
        List<Order> orders = new ArrayList<>();

        String[] parts = query.split("(?i)order\\s+by", 2);
        String conditionPart = parts[0].replaceFirst("(?i)^where", "").trim();
        String orderPart = parts.length > 1 ? parts[1].trim() : "";

        if (!conditionPart.isEmpty()) {
            queryConditions = parseWithPrecedence(conditionPart, substitutions);
        }

        if (!orderPart.isEmpty()) {
            String[] tokens = orderPart.split(",");
            for (String token : tokens) {
                String[] orderTokens = token.trim().split("\\s+");
                String prop = normalizeField(orderTokens[0]);
                boolean desc = orderTokens.length > 1 && orderTokens[1].equalsIgnoreCase("desc");
                orders.add(new Order(prop, desc));
            }
        }

        Class<?> returnType = storageDefinition.entityClass();
        String projectionFieldsRaw = null;

        Pattern returnNewPattern = Pattern.compile("(?i)\\s*returns\\s+new\\s+(\\w+(?:\\.\\w+)*?)\\s*\\(([^)]*)\\)\\s*$");
        Matcher matcher = returnNewPattern.matcher(query);

        if (matcher.find()) {
            String className = matcher.group(1).trim();
            projectionFieldsRaw = matcher.group(2).trim();

            try {
                returnType = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Could not load return class in QQL: " + className, e);
            }
            query = query.substring(0, matcher.start()).trim() + " " + query.substring(matcher.end()).trim();
        }

        val def = new DynamicQueryDefinition(
                method,
                action,
                substitutions,
                queryConditions,
                orders,
                limit,
                distinct,
                false,
                null,
                returnType,
                projectionFieldsRaw
        );

        ParameterBindingUtil.validateNamedParameters(method, def);
        return def;
    }

    private List<QueryCondition> parseWithPrecedence(String conditionPart, List<QuerySubstitution> substitutions) {
        List<QueryCondition> conditions = new ArrayList<>();
        String[] tokens = conditionPart.split("(?i)\\s+(and|or)\\s+");
        Matcher connectorMatcher = Pattern.compile("(?i)\\s+(and|or)\\s+").matcher(conditionPart);
        List<String> connectors = new ArrayList<>();
        while (connectorMatcher.find()) {
            connectors.add(connectorMatcher.group(1).toLowerCase());
        }

        boolean lastWasOr = false;
        for (int i = 0; i < tokens.length; i++) {
            String expr = tokens[i].trim();
            QueryCondition cond = parseSingleCondition(expr, substitutions);
            cond.setOr(lastWasOr);
            conditions.add(cond);
            if (i < connectors.size()) {
                lastWasOr = connectors.get(i).equals("or");
            }
        }
        return conditions;
    }

    private QueryCondition parseSingleCondition(String expr, List<QuerySubstitution> substitutions) {
        Pattern condPattern = Pattern.compile(
                "(lower\\([\\w.]+\\)|upper\\([\\w.]+\\)|[\\w.]+)\\s*" +
                        "(not like|not in|is not null|is null|>=|<=|!=|<>|=|>|<|like|in)\\s*" +
                        "(lower\\([^)]*\\)|upper\\([^)]*\\)|:\\w+|\\?\\d*|\\?|true|false|null|'[^']*')?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = condPattern.matcher(expr);
        if (!m.find()) throw new IllegalArgumentException("Invalid condition expression: " + expr);

        String rawCondition = m.group(0).trim();
        String rawField = m.group(1);
        String operator = m.group(2).toLowerCase();
        String rawValue = m.group(3) != null ? m.group(3).trim() : null;

        String fieldName = normalizeField(extractInner(rawField));
        String fieldFunc = extractCaseFunction(rawField);
        String valueFunc = extractCaseFunction(rawValue);

        CaseFunction fieldCase = toCaseFunction(fieldFunc);
        CaseFunction valueCase = toCaseFunction(valueFunc);
        boolean ignoreCase = fieldCase != CaseFunction.NONE && fieldCase == valueCase;

        AttributePath attribute = new AttributePath(rawField, fieldName, fieldCase);

        Operation op = switch (operator) {
            case "=", "==" -> Operation.EQUAL;
            case "!=", "<>" -> Operation.NOT_EQUAL;
            case ">" -> Operation.GREATER_THAN;
            case ">=" -> Operation.GREATER_THAN_OR_EQUAL;
            case "<" -> Operation.LESS_THAN;
            case "<=" -> Operation.LESS_THAN_OR_EQUAL;
            case "like" -> Operation.LIKE;
            case "not like" -> Operation.NOT_LIKE;
            case "in" -> Operation.IN;
            case "not in" -> Operation.NOT_IN;
            case "is null" -> Operation.IS_NULL;
            case "is not null" -> Operation.IS_NOT_NULL;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
        boolean expectsValue = switch (op) {
            case IS_NULL, IS_NOT_NULL -> false;
            default -> true;
        };

        if (expectsValue && rawValue != null) {
            String innerRaw = extractInner(rawValue);
            if (innerRaw.startsWith("?")) {
                String idx = innerRaw.length() == 1 ? "0" : String.valueOf(Integer.parseInt(innerRaw.substring(1)) - 1);
                substitutions.add(QuerySubstitution.positional(idx, rawValue));
            } else if (innerRaw.startsWith(":")) {
                substitutions.add(QuerySubstitution.named(innerRaw.substring(1), rawValue));
            } else if (rawValue.equalsIgnoreCase("true")) {
                substitutions.add(QuerySubstitution.literal(true, rawValue));
            } else if (rawValue.equalsIgnoreCase("false")) {
                substitutions.add(QuerySubstitution.literal(false, rawValue));
            } else if (rawValue.equalsIgnoreCase("null")) {
                substitutions.add(QuerySubstitution.literal(null, rawValue));
            } else if (rawValue.startsWith("'") && rawValue.endsWith("'")) {
                substitutions.add(QuerySubstitution.literal(rawValue.substring(1, rawValue.length() - 1), rawValue));
            } else {
                throw new IllegalArgumentException("Unsupported value literal: " + rawValue);
            }
        }

        return new QueryCondition(
                rawCondition,
                attribute,
                op,
                rawValue,
                ignoreCase
        );
    }

    private String extractCaseFunction(String expr) {
        if (expr == null) return null;
        Matcher m = Pattern.compile("(?i)(lower|upper)\\(.*\\)").matcher(expr);
        return m.matches() ? m.group(1).toLowerCase() : null;
    }

    private String extractInner(String expr) {
        if (expr == null) return null;
        Matcher m = Pattern.compile("(?i)(?:lower|upper)\\((.+)\\)").matcher(expr);
        return m.matches() ? m.group(1).trim() : expr.trim();
    }

    private CaseFunction toCaseFunction(String func) {
        if (func == null) return CaseFunction.NONE;
        return switch (func.toLowerCase()) {
            case "lower" -> CaseFunction.LOWER;
            case "upper" -> CaseFunction.UPPER;
            default -> CaseFunction.NONE;
        };
    }

    private String normalizeField(String name) {
        if (name == null) return null;
        if (name.contains(".")) return name;
        if (name.contains("_")) return toCamelCase(name);
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private String toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        String[] parts = input.split("_");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) result.append(part.toLowerCase());
            else result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return result.toString();
    }
}