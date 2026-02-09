package com.deepcode.springboard.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Content Security Policy (CSP) 필터
 * XSS 공격 방어를 위한 nonce 기반 보안 헤더를 추가합니다.
 */
@Component
public class CspFilter implements Filter {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 요청마다 암호학적으로 안전한 nonce 생성
        String nonce = generateNonce();
        httpRequest.setAttribute("cspNonce", nonce);

        // CSP 정책 설정
        String cspPolicy = buildCspPolicy(nonce);

        // 1단계: Report-Only 모드로 시작 (실제 차단 안 함, 로그만 기록)
        // 문제 없으면 주석 전환하여 실제 차단 모드로 변경
        httpResponse.setHeader("Content-Security-Policy-Report-Only", cspPolicy);
        // httpResponse.setHeader("Content-Security-Policy", cspPolicy);

        // 추가 보안 헤더
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        chain.doFilter(request, response);
    }

    private String generateNonce() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String buildCspPolicy(String nonce) {
        StringBuilder policy = new StringBuilder();

        // 기본: 같은 도메인만 허용
        policy.append("default-src 'self'; ");

        // 스크립트: 자체 + nonce 기반 인라인 허용 + 신뢰할 수 있는 CDN
        policy.append("script-src 'self' 'nonce-").append(nonce).append("' ")
              .append("https://cdn.jsdelivr.net ")
              .append("https://t1.daumcdn.net ")
              .append("https://i1.daumcdn.net ")
              .append("https://ssl.daumcdn.net; ");

        // 스타일: 자체 + 인라인 허용 (인라인 style 속성이 많아 unsafe-inline 유지)
        policy.append("style-src 'self' 'unsafe-inline' ")
              .append("https://cdn.jsdelivr.net; ");

        // 이미지: 자체 + HTTPS + data URI
        policy.append("img-src 'self' https: data: blob:; ");

        // 폰트: 자체 + CDN
        policy.append("font-src 'self' data: ")
              .append("https://cdn.jsdelivr.net; ");

        // AJAX/WebSocket: 자체 + Google Calendar API
        policy.append("connect-src 'self' https://www.googleapis.com; ");

        // 미디어: 자체만
        policy.append("media-src 'self'; ");

        // 객체 (Flash 등): 없음
        policy.append("object-src 'none'; ");

        // iframe: 같은 도메인 + Daum 우편번호
        policy.append("frame-src 'self' ")
              .append("https://t1.daumcdn.net ")
              .append("https://ssl.daumcdn.net; ");

        // 폼 제출: 자체만
        policy.append("form-action 'self'; ");

        // 기본 문서: 없음
        policy.append("base-uri 'self'; ");

        // 위반 리포트 전송
        policy.append("report-uri /datahub/board/csp-report; ");

        return policy.toString();
    }
}
