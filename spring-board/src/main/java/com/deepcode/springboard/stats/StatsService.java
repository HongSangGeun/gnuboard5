package com.deepcode.springboard.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsMapper statsMapper;

    /**
     * 검색어 통계 저장
     */
    @Transactional
    public void recordSearchKeyword(String keyword, HttpServletRequest request) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        PopularKeyword popularKeyword = new PopularKeyword();
        popularKeyword.setPpWord(keyword.trim());
        popularKeyword.setPpDate(LocalDate.now());
        popularKeyword.setPpIp(getClientIP(request));

        try {
            statsMapper.insertPopularKeyword(popularKeyword);
            log.debug("검색어 통계 저장: keyword={}", keyword);
        } catch (Exception e) {
            log.error("검색어 통계 저장 실패: keyword={}, error={}", keyword, e.getMessage());
        }
    }

    /**
     * 인기 검색어 목록 조회
     */
    public List<PopularKeyword> getPopularKeywords(int page, int limit) {
        int offset = (page - 1) * limit;
        return statsMapper.getPopularKeywords(limit, offset);
    }

    /**
     * 인기 검색어 개수
     */
    public int getPopularKeywordCount() {
        return statsMapper.getPopularKeywordCount();
    }

    /**
     * 인기 검색어 순위 (기간별)
     */
    public List<PopularKeywordRank> getPopularKeywordRank(LocalDate fromDate, LocalDate toDate, int limit) {
        return statsMapper.getPopularKeywordRank(fromDate, toDate, limit);
    }

    /**
     * 오늘의 인기 검색어 TOP 10
     */
    public List<PopularKeywordRank> getTodayPopularKeywords() {
        LocalDate today = LocalDate.now();
        return statsMapper.getPopularKeywordRank(today, today, 10);
    }

    /**
     * 주간 인기 검색어 TOP 10
     */
    public List<PopularKeywordRank> getWeeklyPopularKeywords() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        return statsMapper.getPopularKeywordRank(weekAgo, today, 10);
    }

    /**
     * 월간 인기 검색어 TOP 10
     */
    public List<PopularKeywordRank> getMonthlyPopularKeywords() {
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusMonths(1);
        return statsMapper.getPopularKeywordRank(monthAgo, today, 10);
    }

    /**
     * 인기 검색어 삭제
     */
    @Transactional
    public void deletePopularKeyword(Integer ppId) {
        statsMapper.deletePopularKeyword(ppId);
    }

    /**
     * 오래된 인기 검색어 삭제 (일괄)
     */
    @Transactional
    public void deleteOldPopularKeywords(int daysToKeep) {
        LocalDate beforeDate = LocalDate.now().minusDays(daysToKeep);
        statsMapper.deletePopularKeywordsBefore(beforeDate);
        log.info("오래된 인기 검색어 삭제: before={}", beforeDate);
    }

    /**
     * 클라이언트 IP 주소 가져오기
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
        return ip;
    }
}
