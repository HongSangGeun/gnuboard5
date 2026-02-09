package com.deepcode.springboard.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            // 세션 쿠키 보안 설정
            SessionCookieConfig sessionCookie = servletContext.getSessionCookieConfig();
            sessionCookie.setHttpOnly(true);  // JavaScript로 쿠키 접근 차단 (XSS 방지)
            sessionCookie.setSecure(false);   // 개발 환경: false, 운영 환경: true (HTTPS 필수)
            sessionCookie.setMaxAge(60 * 60 * 2); // 2시간

            // URL 리라이팅 비활성화 (세션 ID가 URL에 노출되는 것 방지)
            servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));

            // 세션 타임아웃 설정 (30분)
            servletContext.getSessionCookieConfig().setMaxAge(1800);
        };
    }
}
