package xyz.quartzframework.data.entity;

import java.lang.reflect.Field;

public record EntityDefinition(
        Class<?> entityClass,
        Class<?> idClass,
        Field idField,
        String className,
        String entityName
) { }