package com.deepcode.springboard.visit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 접속자 통계 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 현재 접속자 수 조회
     * 최근 5분 이내 접속한 고유 IP 수
     */
    public int getCurrentVisitorCount() {
        try {
            String sql = """
                SELECT COUNT(DISTINCT vi_ip) FROM g5_visit
                WHERE vi_datetime >= DATE_SUB(NOW(), INTERVAL 5 MINUTE)
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("현재 접속자 수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 오늘 방문자 수 조회
     */
    public int getTodayVisitorCount() {
        try {
            String sql = """
                SELECT IFNULL(
                    (SELECT vs_count FROM g5_visit_sum WHERE vs_date = CURDATE()),
                    0
                ) as count
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("오늘 방문자 수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 어제 방문자 수 조회
     */
    public int getYesterdayVisitorCount() {
        try {
            String sql = """
                SELECT IFNULL(
                    (SELECT vs_count FROM g5_visit_sum WHERE vs_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
                    0
                ) as count
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("어제 방문자 수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 전체 방문자 수 조회
     */
    public int getTotalVisitorCount() {
        try {
            String sql = "SELECT COALESCE(SUM(vs_count), 0) FROM g5_visit_sum";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("전체 방문자 수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 최고 방문자 수 조회
     */
    public int getMaxVisitorCount() {
        try {
            String sql = "SELECT COALESCE(MAX(vs_count), 0) FROM g5_visit_sum";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("최고 방문자 수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 접속자 통계 정보
     */
    public VisitStats getVisitStats() {
        VisitStats stats = new VisitStats();
        stats.setCurrentCount(getCurrentVisitorCount());
        stats.setTodayCount(getTodayVisitorCount());
        stats.setYesterdayCount(getYesterdayVisitorCount());
        stats.setTotalCount(getTotalVisitorCount());
        stats.setMaxCount(getMaxVisitorCount());
        return stats;
    }
}
