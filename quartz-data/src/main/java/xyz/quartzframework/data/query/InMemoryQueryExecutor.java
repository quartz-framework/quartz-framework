package xyz.quartzframework.data.query;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import xyz.quartzframework.data.entity.Attribute;
import xyz.quartzframework.data.page.Page;
import xyz.quartzframework.data.page.Pagination;
import xyz.quartzframework.data.util.ParameterBindingUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
public class InMemoryQueryExecutor<E> implements QueryExecutor<E> {

    private final Collection<E> source;

    @Getter
    private final Class<E> entityType;

    public InMemoryQueryExecutor(Collection<E> source, Class<E> entityType) {
        this.source = List.copyOf(source);
        this.entityType = entityType;
    }

    @Override
    public <R> List<R> find(DynamicQueryDefinition query, Object[] args) {
        List<E> result = new ArrayList<>(source);

        List<List<Predicate<E>>> orGroups = new ArrayList<>();
        List<Predicate<E>> currentGroup = new ArrayList<>();
        List<QuerySubstitution> substitutions = query.querySubstitutions();
        int subIndex = 0;

        for (QueryCondition condition : query.queryConditions()) {
            QuerySubstitution sub = null;
            Object value = null;

            boolean expectsValue = switch (condition.getOperation()) {
                case IS_NULL, IS_NOT_NULL -> false;
                default -> true;
            };

            if (expectsValue) {
                sub = subIndex < substitutions.size() ? substitutions.get(subIndex) : null;
                subIndex++;

                if (sub == null) {
                    throw new ParameterBindingException("No substitution found for condition: " + condition);
                }

                if (sub.isLiteral()) {
                    value = sub.getLiteralValue();
                } else if (sub.isNamed()) {
                    value = ParameterBindingUtil.findNamedParameter(query.method(), sub.getNameOrIndex(), args);
                } else {
                    String nameOrIndex = sub.getNameOrIndex();
                    if (nameOrIndex == null) {
                        throw new ParameterBindingException("Missing substitution index for positional parameter");
                    }

                    int idx;
                    try {
                        idx = Integer.parseInt(nameOrIndex);
                    } catch (NumberFormatException ex) {
                        throw new ParameterBindingException("Invalid substitution index: ?" + nameOrIndex, ex);
                    }

                    if (idx < 0 || idx >= args.length) {
                        throw new ParameterBindingException("Missing argument for parameter index: ?" + idx);
                    }

                    value = args[idx];
                }
            }

            Object finalValue = value;
            Predicate<E> predicate = entity -> {
                try {
                    Object fieldValue = getNestedFieldValue(entity, condition.getAttribute().name());
                    return match(fieldValue, condition.getOperation(), finalValue, condition);
                } catch (Exception e) {
                    log.warn("Failed to evaluate condition on entity: {}", entity, e);
                    return false;
                }
            };

            if (condition.isOr()) {
                if (!currentGroup.isEmpty()) {
                    orGroups.add(currentGroup);
                    currentGroup = new ArrayList<>();
                }
            }
            currentGroup.add(predicate);
        }
        if (!currentGroup.isEmpty()) {
            orGroups.add(currentGroup);
        }

        Predicate<E> finalPredicate = orGroups.stream()
                .map(group -> group.stream().reduce(x -> true, Predicate::and))
                .reduce(x -> false, Predicate::or);

        result = result.stream().filter(finalPredicate).collect(Collectors.toList());

        if (!query.orders().isEmpty()) {
            result.sort((a, b) -> {
                for (Order order : query.orders()) {
                    try {
                        Object va = getNestedFieldValue(a, order.property());
                        Object vb = getNestedFieldValue(b, order.property());
                        if (va == null && vb == null) continue;
                        if (va == null) return order.descending() ? 1 : -1;
                        if (vb == null) return order.descending() ? -1 : 1;
                        if (va instanceof Comparable<?> && va.getClass().equals(vb.getClass())) {
                            @SuppressWarnings("unchecked")
                            Comparable<Object> cmpA = (Comparable<Object>) va;
                            int cmp = cmpA.compareTo(vb);
                            if (cmp != 0) return order.descending() ? -cmp : cmp;
                        }
                    } catch (Exception e) {
                        log.warn("Ordering failed for properties: {}", order.property(), e);
                    }
                }
                return 0;
            });
        }

        if (query.distinct()) {
            result = new ArrayList<>(new LinkedHashSet<>(result));
        }

        if (query.limit() != null && query.limit() > 0 && result.size() > query.limit()) {
            result = result.subList(0, query.limit());
        }

        if (!query.returnType().isAssignableFrom(getEntityType()) && query.projectionFields() != null) {
            result = applyProjection(result, query);
        }

        return (List<R>) result;
    }

    @Override
    public <R> Page<R> find(DynamicQueryDefinition query, Object[] args, Pagination pagination) {
        List<R> results = find(query, args);
        int total = results.size();
        int from = Math.min(pagination.offset(), total);
        int to = Math.min(from + pagination.size(), total);
        List<R> pageItems = results.subList(from, to);
        return Page.of(pageItems, pagination, total);
    }

    @Override
    public long count(DynamicQueryDefinition query, Object[] args) {
        return find(query, args).size();
    }

    @Override
    public boolean exists(DynamicQueryDefinition query, Object[] args) {
        return !find(query, args).isEmpty();
    }

    private boolean match(Object fieldValue, Operation operation, Object expectedValue, QueryCondition condition) {
        try {
            boolean ignoreCase = condition.isIgnoreCase();
            val attribute = condition.getAttribute();
            if (ignoreCase && fieldValue instanceof String f && expectedValue instanceof String e) {
                fieldValue = attribute.applyCaseFunction(f);
                expectedValue = attribute.applyCaseFunction(e);
            }
            if (operation == Operation.EQUAL) {
                return Objects.equals(fieldValue, expectedValue);
            }
            if (operation == Operation.NOT_EQUAL) {
                return !Objects.equals(fieldValue, expectedValue);
            }
            if (operation == Operation.GREATER_THAN && fieldValue instanceof Comparable) {
                assert expectedValue != null;
                return ((Comparable<Object>) fieldValue).compareTo(expectedValue) > 0;
            }
            if (operation == Operation.GREATER_THAN_OR_EQUAL && fieldValue instanceof Comparable && expectedValue != null) {
                return ((Comparable<Object>) fieldValue).compareTo(expectedValue) >= 0;
            }
            if (operation == Operation.LESS_THAN && fieldValue instanceof Comparable) {
                assert expectedValue != null;
                return ((Comparable<Object>) fieldValue).compareTo(expectedValue) < 0;
            }
            if (operation == Operation.LESS_THAN_OR_EQUAL && fieldValue instanceof Comparable && expectedValue != null) {
                return ((Comparable<Object>) fieldValue).compareTo(expectedValue) <= 0;
            }
            if ((operation == Operation.LIKE || operation == Operation.NOT_LIKE)
                    && fieldValue instanceof String str
                    && expectedValue instanceof String pattern) {
                boolean matches;
                if (!pattern.contains("%") && !pattern.contains("_")) {
                    matches = ignoreCase
                            ? str.toLowerCase().contains(pattern.toLowerCase())
                            : str.contains(pattern);
                } else {
                    String regex = likeToRegex(pattern);
                    Pattern p = ignoreCase
                            ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                            : Pattern.compile(regex);
                    matches = p.matcher(str).matches();
                }
                return (operation == Operation.LIKE) == matches;
            }
            if (operation == Operation.IS_NULL) return fieldValue == null;
            if (operation == Operation.IS_NOT_NULL) return fieldValue != null;
            if (operation == Operation.IN && expectedValue instanceof Collection<?> collection) {
                if (fieldValue == null) return false;
                return collection.contains(fieldValue);
            }
            if (operation == Operation.NOT_IN && expectedValue instanceof Collection<?> collection) {
                if (fieldValue == null) return true;
                return !collection.contains(fieldValue);
            }
        } catch (Exception e) {
            log.warn("Failed to match: fieldValue={}, operation={}, expectedValue={}", fieldValue, operation, expectedValue, e);
        }

        return false;
    }

    private Object getNestedFieldValue(Object root, String path) throws Exception {
        String[] parts = path.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (current == null) return null;
            Field field = findField(current.getClass(), part);
            field.setAccessible(true);
            current = field.get(current);
        }
        return current;
    }

    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(name)) return field;
                val alias = field.getAnnotation(Attribute.class);
                if (alias != null && alias.value().equals(name)) return field;
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException("Field or @Attribute '" + name + "' not found");
    }

    private String likeToRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        for (char c : pattern.toCharArray()) {
            switch (c) {
                case '%': regex.append(".*"); break;
                case '_': regex.append('.'); break;
                case '\\': regex.append("\\\\"); break;
                case '^', '$', '.', '|', '?', '*', '+', '(', ')', '[', '{':
                    regex.append('\\').append(c); break;
                default: regex.append(c);
            }
        }
        regex.insert(0, "^");
        regex.append("$");
        return regex.toString();
    }

    @SuppressWarnings("unchecked")
    private <R> List<R> applyProjection(List<E> entities, DynamicQueryDefinition query) {
        try {
            assert query.projectionFields() != null;
            String[] fieldNames = query.projectionFields().split("\\s*,\\s*");
            Class<?> dtoClass = query.returnType();
            Class<?>[] paramTypes = new Class<?>[fieldNames.length];
            for (int i = 0; i < fieldNames.length; i++) {
                paramTypes[i] = resolveNestedFieldType(getEntityType(), fieldNames[i]);
            }
            var constructor = dtoClass.getConstructor(paramTypes);
            List<Object> projected = new ArrayList<>();
            for (E entity : entities) {
                Object[] values = new Object[fieldNames.length];
                for (int i = 0; i < fieldNames.length; i++) {
                    values[i] = getNestedFieldValue(entity, fieldNames[i]);
                }
                Object dto = constructor.newInstance(values);
                projected.add(dto);
            }

            return (List<R>) projected;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to project result to " + query.returnType().getName(), e);
        }
    }

    private Class<?> resolveNestedFieldType(Class<?> rootClass, String path) throws NoSuchFieldException {
        Class<?> current = rootClass;
        for (String part : path.split("\\.")) {
            Field field = findField(current, part);
            current = field.getType();
        }
        return current;
    }
}