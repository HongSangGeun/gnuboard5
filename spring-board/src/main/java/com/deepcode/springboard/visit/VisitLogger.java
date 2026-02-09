package com.deepcode.springboard.visit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 접속자 로그 기록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitLogger {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 접속자 로그 기록 (비동기)
     */
    @Async
    public void logVisit(HttpServletRequest request) {
        try {
            String ip = getClientIP(request);
            String referer = request.getHeader("Referer");
            String userAgent = request.getHeader("User-Agent");

            // 접속자 상세 로그 저장
            String sql = """
                INSERT INTO g5_visit (vi_ip, vi_datetime, vi_referer, vi_agent)
                VALUES (?, NOW(), ?, ?)
                """;
            jdbcTemplate.update(sql, ip, referer, userAgent);

            // 일별 접속자 집계 업데이트 (중복 IP는 하루에 한 번만 카운트)
            updateDailyVisitCount(ip);

            log.debug("접속자 로그 기록: ip={}", ip);
        } catch (Exception e) {
            log.error("접속자 로그 기록 실패: {}", e.getMessage());
        }
    }

    /**
     * 일별 접속자 집계 업데이트
     */
    private void updateDailyVisitCount(String ip) {
        try {
            LocalDate today = LocalDate.now();

            // 오늘 이 IP가 이미 기록되었는지 확인
            String checkSql = """
                SELECT COUNT(*) FROM g5_visit
                WHERE vi_ip = ? AND DATE(vi_datetime) = ?
                """;
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, ip, today);

            // 처음 방문한 IP인 경우에만 집계 업데이트
            if (count != null && count == 1) {
                String updateSql = """
                    INSERT INTO g5_visit_sum (vs_date, vs_count)
                    VALUES (?, 1)
                    ON DUPLICATE KEY UPDATE vs_count = vs_count + 1
                    """;
                jdbcTemplate.update(updateSql, today);
            }
        } catch (Exception e) {
            log.error("일별 접속자 집계 업데이트 실패: {}", e.getMessage());
        }
    }

    /**
     * 클라이언트 실제 IP 추출
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For의 경우 여러 IP가 있을 수 있으므로 첫 번째 IP만 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
