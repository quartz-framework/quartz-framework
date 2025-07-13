package xyz.quartzframework.data.storage;

import xyz.quartzframework.data.page.Page;
import xyz.quartzframework.data.page.Pagination;

public interface PageableStorage<E> {

    Page<E> findAll(Pagination pagination);

}