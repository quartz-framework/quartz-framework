package xyz.quartzframework.data.storage;

import lombok.Getter;
import xyz.quartzframework.data.page.Page;
import xyz.quartzframework.data.page.Pagination;
import xyz.quartzframework.data.page.Sort;
import xyz.quartzframework.data.util.IdentityUtil;
import xyz.quartzframework.data.util.SortUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapStorage<E, ID> implements InMemoryStorage<E, ID> {

    private final Map<ID, E> storage = new ConcurrentHashMap<>();

    @Getter
    private final Class<E> entityClass;

    @Getter
    private final Class<ID> idClass;

    public HashMapStorage(Class<E> entityClass, Class<ID> idClass) {
        this.idClass = idClass;
        this.entityClass = entityClass;
    }

    @Override
    public Optional<E> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public boolean exists(ID id) {
        return storage.containsKey(id);
    }

    @Override
    public Page<E> findAll(Pagination pagination) {
        return Page.fromList(storage.values().stream().toList(), pagination);
    }

    @Override
    public E save(E entity) {
        ID id = IdentityUtil.extractId(entity, idClass);
        storage.put(id, entity);
        return entity;
    }

    @Override
    public List<E> save(Iterable<E> entities) {
        List<E> saved = new ArrayList<>();
        for (E entity : entities) {
            ID id = IdentityUtil.extractId(entity, idClass);
            storage.put(id, entity);
            saved.add(entity);
        }
        return saved;
    }

    @Override
    public void deleteById(ID id) {
        storage.remove(id);
    }

    @Override
    public void delete(E entity) {
        ID id = IdentityUtil.extractId(entity, idClass);
        storage.remove(id);
    }

    @Override
    public void delete(Iterable<E> entities) {
        for (E entity : entities) {
            delete(entity);
        }
    }


    @Override
    public List<E> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<E> findAll(Sort sort) {
        List<E> result = new ArrayList<>(storage.values());
        SortUtil.sortList(result, sort);
        return result;
    }
}