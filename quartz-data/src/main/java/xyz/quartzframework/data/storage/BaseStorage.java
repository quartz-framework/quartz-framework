package xyz.quartzframework.data.storage;

import java.util.List;
import java.util.Optional;

public interface BaseStorage<E, ID> {

    Optional<E> findById(ID id);

    long count();

    boolean exists(ID id);

    E save(E entity);

    List<E> save(Iterable<E> entities);

    void deleteById(ID id);

    void delete(E entity);

    void delete(Iterable<E> entities);

    Class<E> getEntityClass();

    Class<ID> getIdClass();

}