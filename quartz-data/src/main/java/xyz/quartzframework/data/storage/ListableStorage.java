package xyz.quartzframework.data.storage;

import xyz.quartzframework.data.page.Sort;

import java.util.List;

public interface ListableStorage<E> {

    List<E> findAll();

    List<E> findAll(Sort sort);

}