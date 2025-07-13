package xyz.quartzframework.data.page;

public interface Pagination {

    int page();

    int size();

    default int offset() {
        return page() * size();
    }

    Sort sort();

    static Pagination of(int page, int size) {
        return of(page, size, Sort.unsorted());
    }

    static Pagination of(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }

    static Pagination ofSize(int size) {
        return of(0, size);
    }

    static Pagination unpaged() {
        return new Pagination() {
            @Override
            public int page() {
                return 0;
            }

            @Override
            public int size() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int offset() {
                return 0;
            }

            @Override
            public Sort sort() {
                return Sort.unsorted();
            }
        };
    }

    static boolean isUnpaged(Pagination pagination) {
        return pagination.size() == Integer.MAX_VALUE && pagination.offset() == 0;
    }
}