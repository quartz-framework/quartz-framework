package xyz.quartzframework.data.page;

import java.util.Objects;

public record PageRequest(int page, int size, Sort sort) implements Pagination {

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        Objects.requireNonNull(sort, "sort must not be null");
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, Sort.unsorted());
    }

    public static PageRequest of(int page, int size, Sort sort) {
        return new PageRequest(page, size, sort);
    }
}