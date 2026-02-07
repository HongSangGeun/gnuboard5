package com.gnuboard.springboard.common;

import java.util.List;

public class PagedResult<T> {
    private final List<T> items;
    private final int page;
    private final int size;
    private final int totalCount;
    // Legacy Thymeleaf compatibility: `${result.total}` direct field access
    private final int total;

    public PagedResult(List<T> items, int page, int size, int totalCount) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
        this.total = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalCount() {
        return totalCount;
    }

    // Legacy template compatibility: some screens still reference result.total
    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / (double) size);
    }
}
