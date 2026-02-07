package com.gnuboard.springboard.config;

import com.gnuboard.springboard.security.CsrfTokenManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;

public class CsrfInterceptor implements HandlerInterceptor {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(true);
        String expectedToken = CsrfTokenManager.ensureToken(session);

        if (SAFE_METHODS.contains(request.getMethod())) {
            return true;
        }
        if (isEditorUploadPath(request)) {
            return true;
        }

        String providedToken = request.getHeader("X-CSRF-TOKEN");
        if (!StringUtils.hasText(providedToken)) {
            providedToken = request.getParameter("_csrf");
        }
        if (!CsrfTokenManager.matches(expectedToken, providedToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token mismatch");
            return false;
        }
        return true;
    }

    private boolean isEditorUploadPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri;
        if (StringUtils.hasText(contextPath) && uri.startsWith(contextPath)) {
            path = uri.substring(contextPath.length());
        }
        return path.contains("/plugin/editor/");
    }
}
