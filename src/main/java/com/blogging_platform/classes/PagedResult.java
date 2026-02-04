package com.blogging_platform.classes;

import java.util.List;

public record PagedResult<T>(
    List<T> content,
    int page,
    int size,
    long totalElements
) {
    public int getTotalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean isFirst() { return page == 0; }
    public boolean isLast()  { return page >= getTotalPages() - 1; }
    public boolean hasNext() { return page + 1 < getTotalPages(); }
    public boolean hasPrevious() { return page > 0; }
}
