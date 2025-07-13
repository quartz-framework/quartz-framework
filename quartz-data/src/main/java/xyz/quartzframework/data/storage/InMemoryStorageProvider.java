package xyz.quartzframework.data.storage;

import xyz.quartzframework.data.query.InMemoryQueryExecutor;
import xyz.quartzframework.data.query.QueryExecutor;

public class InMemoryStorageProvider implements StorageProvider {

    @Override
    public <E, ID> HashMapStorage<E, ID> create(Class<E> entity, Class<ID> id) {
        return new HashMapStorage<>(entity, id);
    }

    @Override
    public <E, ID> QueryExecutor<E> getQueryExecutor(SimpleStorage<E, ID> storage) {
        return new InMemoryQueryExecutor<>(storage.findAll(), storage.getEntityClass());
    }
}