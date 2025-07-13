package xyz.quartzframework.data.storage;

import xyz.quartzframework.data.annotation.SuperStorage;

@SuperStorage(InMemoryStorageProvider.class)
public interface InMemoryStorage<E, ID> extends SimpleStorage<E, ID> {

}