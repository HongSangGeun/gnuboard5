package com.deepcode.springboard.stats;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PopularKeyword {
    private Integer ppId;
    private String ppWord;      // 검색어
    private LocalDate ppDate;   // 검색일
    private String ppIp;        // IP 주소
}
