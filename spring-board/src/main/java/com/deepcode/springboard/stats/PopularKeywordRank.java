package com.deepcode.springboard.stats;

import lombok.Data;

@Data
public class PopularKeywordRank {
    private String ppWord;      // 검색어
    private Long cnt;           // 검색 횟수
}
