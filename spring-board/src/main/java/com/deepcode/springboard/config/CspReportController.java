package com.deepcode.springboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * CSP 위반 리포트를 받는 컨트롤러
 * 브라우저가 CSP 위반을 감지하면 이 엔드포인트로 리포트를 전송합니다.
 */
@RestController
public class CspReportController {

    private static final Logger log = LoggerFactory.getLogger(CspReportController.class);

    @PostMapping(value = "/csp-report", consumes = {"application/csp-report", "application/json"})
    public void handleCspReport(@RequestBody String report) {
        // CSP 위반 사항을 로그로 기록
        log.warn("CSP Violation Report: {}", report);
    }
}
