package xyz.quartzframework.data.storage;

import xyz.quartzframework.data.query.QueryExecutor;

public interface StorageProvider {

    <E, ID> SimpleStorage<E, ID> create(Class<E> entity, Class<ID> id);

    <E, ID> QueryExecutor<E> getQueryExecutor(SimpleStorage<E, ID> storage);

}