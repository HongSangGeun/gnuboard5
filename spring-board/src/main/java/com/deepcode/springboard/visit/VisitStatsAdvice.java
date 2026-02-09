package com.deepcode.springboard.visit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 페이지에 접속자 통계 정보 제공
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class VisitStatsAdvice {

    private final VisitService visitService;

    /**
     * 모든 컨트롤러에 접속자 통계 정보 추가
     */
    @ModelAttribute("visitStats")
    public VisitStats visitStats() {
        try {
            return visitService.getVisitStats();
        } catch (Exception e) {
            log.error("접속자 통계 조회 실패: {}", e.getMessage());
            // 에러 시 기본값 반환
            return new VisitStats();
        }
    }
}
