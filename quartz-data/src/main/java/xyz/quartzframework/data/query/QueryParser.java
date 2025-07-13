package xyz.quartzframework.data.query;

import xyz.quartzframework.data.storage.StorageDefinition;

import java.lang.reflect.Method;

public interface QueryParser {

    DynamicQueryDefinition parse(Method method, StorageDefinition storageDefinition);

    boolean supports(Method method);

    String queryString(Method method);

}