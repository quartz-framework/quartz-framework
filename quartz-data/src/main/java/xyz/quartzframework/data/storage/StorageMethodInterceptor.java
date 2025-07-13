package xyz.quartzframework.data.storage;

import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import xyz.quartzframework.data.page.Page;
import xyz.quartzframework.data.page.Pagination;
import xyz.quartzframework.data.query.DynamicQueryDefinition;
import xyz.quartzframework.data.query.Query;
import xyz.quartzframework.data.query.QueryExecutor;
import xyz.quartzframework.data.query.QueryParser;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class StorageMethodInterceptor<E, ID> implements MethodInterceptor {

    private final QueryParser queryParser;

    private final QueryExecutor<E> executor;

    private final Class<E> entityType;

    private final Class<ID> idType;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (method.getDeclaringClass().equals(Object.class)
                || method.getName().equals("toString")
                || method.getName().equals("equals")
                || method.getName().equals("hashCode")) {
            return invocation.proceed();
        }
        if (method.isDefault()) {
            return invocation.proceed();
        }
        if (!isDynamicMethod(method)) return invocation.proceed();
        DynamicQueryDefinition query = queryParser.parse(method, new StorageDefinition(entityType, idType));
        String queryString = queryParser.queryString(method);
        validateReturnType(method, query);
        Object[] args = invocation.getArguments();
        long dynamicSubstitutions = query
                .querySubstitutions()
                .stream()
                .filter(sub -> !sub.isLiteral())
                .count();
        if (args.length < dynamicSubstitutions) {
            throw new IllegalStateException("Expected " + dynamicSubstitutions + " arguments for query '" + queryString + "', but got " + args.length);
        }
        Class<?> returnType = method.getReturnType();
        return switch (query.action()) {
            case FIND -> handleFind(query, returnType, args, queryString);
            case COUNT -> {
                if (isNumeric(returnType)) yield executor.count(query, args);
                throw new UnsupportedOperationException("COUNT must return numeric type: " + method.getName());
            }
            case EXISTS -> {
                if (returnType == boolean.class || returnType == Boolean.class) yield executor.exists(query, args);
                throw new UnsupportedOperationException("EXISTS must return boolean: " + method.getName());
            }
        };
    }

    private Object handleFind(DynamicQueryDefinition query, Class<?> returnType, Object[] args, String methodName) {
        if (Page.class.isAssignableFrom(returnType)) {
            Pagination pagination = Arrays.stream(args)
                    .filter(Pagination.class::isInstance)
                    .map(Pagination.class::cast)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Pagination required for paged method"));
            return executor.find(query, args, pagination);
        }
        List<?> results = executor.find(query, args);
        if (Set.class.isAssignableFrom(returnType)) return new HashSet<>(results);
        if (Stream.class.isAssignableFrom(returnType)) return results.stream();
        if (List.class.isAssignableFrom(returnType)) return results;
        if (Optional.class.isAssignableFrom(returnType)) return results.stream().findFirst();
        if (entityType.isAssignableFrom(returnType)) {
            return results.stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No result found for: " + methodName));
        }
        throw new UnsupportedOperationException("Unsupported return type in FIND: " + returnType.getName());
    }

    private boolean isNumeric(Class<?> type) {
        return type == long.class || type == Long.class || Number.class.isAssignableFrom(type);
    }

    private boolean isDynamicMethod(Method method) {
        if (method.isAnnotationPresent(Query.class)) return true;
        try {
            SimpleStorage.class.getMethod(method.getName(), method.getParameterTypes());
            return false;
        } catch (NoSuchMethodException e) {
            String name = method.getName();
            return name.startsWith("find") ||
                    name.startsWith("count") ||
                    name.startsWith("exists");
        }
    }

    private void validateReturnType(Method method, DynamicQueryDefinition query) {
        Class<?> returnType = method.getReturnType();
        String methodName = method.getName();
        switch (query.action()) {
            case FIND -> {
                if (!isSupportedFindReturnType(returnType)) {
                    throw new UnsupportedOperationException("FIND return type not supported: " + returnType.getName());
                }
            }
            case EXISTS -> {
                if (!(returnType == boolean.class || returnType == Boolean.class)) {
                    throw new UnsupportedOperationException("EXISTS must return boolean: " + methodName);
                }
            }
            case COUNT -> {
                if (!(returnType == long.class || returnType == Long.class || Number.class.isAssignableFrom(returnType))) {
                    throw new UnsupportedOperationException("COUNT must return numeric type: " + methodName);
                }
            }
        }
    }

    private boolean isSupportedFindReturnType(Class<?> returnType) {
        return List.class.isAssignableFrom(returnType) ||
                Set.class.isAssignableFrom(returnType) ||
                Stream.class.isAssignableFrom(returnType) ||
                Optional.class.isAssignableFrom(returnType) ||
                Page.class.isAssignableFrom(returnType) ||
                entityType.isAssignableFrom(returnType);
    }
}
