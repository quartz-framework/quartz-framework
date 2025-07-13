package xyz.quartzframework.data.storage;

public record StorageDefinition(
        Class<?> entityClass,
        Class<?> idClass
) { }