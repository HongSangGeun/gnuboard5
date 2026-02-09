package com.deepcode.springboard.visit;

import lombok.Data;

/**
 * 접속자 통계 정보
 */
@Data
public class VisitStats {
    private int currentCount;    // 현재 접속자
    private int todayCount;      // 오늘 방문자
    private int yesterdayCount;  // 어제 방문자
    private int totalCount;      // 전체 방문자
    private int maxCount;        // 최고 방문자
}
