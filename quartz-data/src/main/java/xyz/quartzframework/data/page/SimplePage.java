package xyz.quartzframework.data.page;

import java.util.List;

public record SimplePage<T>(
    List<T> content,
    int page,
    int size,
    long totalElements
) implements Page<T> {

    @Override
    public int totalPages() {
        if (size == 0) return 0;
        return (int) Math.ceil((double) totalElements / size);
    }
}