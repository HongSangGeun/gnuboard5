package com.deepcode.springboard.visit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 접속자 로그 인터셉터
 * 모든 요청에 대해 접속자 정보를 기록합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitInterceptor implements HandlerInterceptor {

    private final VisitLogger visitLogger;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 정적 리소스는 로그에서 제외
        String requestUri = request.getRequestURI();
        if (isStaticResource(requestUri)) {
            return true;
        }

        // 관리자 페이지는 로그에서 제외 (선택사항)
        if (requestUri.startsWith("/mgmt")) {
            return true;
        }

        // 비동기로 접속자 로그 기록
        visitLogger.logVisit(request);

        return true;
    }

    /**
     * 정적 리소스 여부 확인
     */
    private boolean isStaticResource(String uri) {
        return uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/images/") ||
               uri.startsWith("/static/") ||
               uri.startsWith("/webjars/") ||
               uri.startsWith("/favicon.ico") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg") ||
               uri.endsWith(".jpeg") ||
               uri.endsWith(".gif") ||
               uri.endsWith(".svg") ||
               uri.endsWith(".ico");
    }
}
