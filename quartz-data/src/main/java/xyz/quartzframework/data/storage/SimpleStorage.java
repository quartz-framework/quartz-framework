package xyz.quartzframework.data.storage;

public interface SimpleStorage<E, ID> extends
        BaseStorage<E, ID>,
        ListableStorage<E>,
        PageableStorage<E> {

}
