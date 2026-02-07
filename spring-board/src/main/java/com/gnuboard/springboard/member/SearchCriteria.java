package com.gnuboard.springboard.member;

import lombok.Data;

@Data
public class SearchCriteria {
    private String sfl; // search field
    private String stx; // search text
    private String sst; // sort field
    private String sod; // sort order
    private int page;
    private int limit;
    private int offset;

    public int getOffset() {
        return (page - 1) * limit;
    }
}
