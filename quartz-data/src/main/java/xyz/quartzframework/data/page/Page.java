package xyz.quartzframework.data.page;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Page<T> {

    List<T> content();

    int page();

    int size();

    long totalElements();

    int totalPages();

    default boolean isEmpty() {
        return content().isEmpty();
    }

    default boolean hasNext() {
        return page() + 1 < totalPages();
    }

    default boolean isLast() {
        return !hasNext();
    }

    default Stream<T> stream() {
        return content().stream();
    }

    default <R> Page<R> map(Function<T, R> mapper) {
        List<R> mapped = stream().map(mapper).toList();
        return Page.of(mapped, page(), size(), totalElements());
    }

    default Page<T> filter(Predicate<? super T> filter) {
        List<T> filtered = stream().filter(filter).toList();
        return Page.of(filtered, page(), size(), totalElements());
    }

    static <T> Page<T> of(List<T> content, Pagination pagination, long totalElements) {
        return new SimplePage<>(content, pagination.page(), pagination.size(), totalElements);
    }

    static <T> Page<T> of(List<T> content, int page, int size, long totalElements) {
        return new SimplePage<>(content, page, size, totalElements);
    }

    static <T> Page<T> fromList(List<T> fullList, Pagination pagination) {
        int offset = pagination.offset();
        int end = Math.min(offset + pagination.size(), fullList.size());
        List<T> pagedContent = offset >= fullList.size()
                ? List.of()
                : fullList.subList(offset, end);

        return Page.of(pagedContent, pagination.page(), pagination.size(), fullList.size());
    }

    static <T> Page<T> fromStream(Stream<T> stream, Pagination pagination) {
        return fromList(stream.toList(), pagination);
    }
}