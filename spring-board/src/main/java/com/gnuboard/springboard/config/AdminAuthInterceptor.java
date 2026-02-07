package com.gnuboard.springboard.config;

import com.gnuboard.springboard.common.SessionConst;
import com.gnuboard.springboard.member.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class AdminAuthInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AdminAuthInterceptor.class);
    private final G5ConfigService configService;

    public AdminAuthInterceptor(G5ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        LoginUser user = null;
        if (session != null) {
            Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (value instanceof LoginUser) {
                user = (LoginUser) value;
            }
        }
        if (user == null) {
            log.warn("AdminAuth denied: no-login uri={} servletPath={}", request.getRequestURI(), request.getServletPath());
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String servletPath = normalizePath(request);
        String uriPath = normalizeRequestUriPath(request);
        if (isBoardAdminPath(servletPath) || isBoardAdminPath(uriPath)) {
            // /mgmt/board** 경로는 컨트롤러에서 게시판 단위 권한(최고관리자/게시판관리자)을 검증한다.
            return true;
        }
        if (!configService.isAdmin(user)) {
            log.warn("AdminAuth denied: not-admin userId={} level={} uri={} servletPath={} normalized={} uriNormalized={}",
                    user.getId(), user.getLevel(), request.getRequestURI(), request.getServletPath(), servletPath, uriPath);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private String normalizePath(HttpServletRequest request) {
        // servletPath는 context-path가 제거된 경로이므로 권한 판정 기준으로 가장 안정적이다.
        String path = request.getServletPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            String contextPath = request.getContextPath();
            String uri = request.getRequestURI();
            path = uri;
            if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
        }
        if (path == null || path.isBlank()) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        int matrixParamPos = path.indexOf(';');
        if (matrixParamPos >= 0) {
            path = path.substring(0, matrixParamPos);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private String normalizeRequestUriPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path == null || path.isBlank()) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        int matrixParamPos = path.indexOf(';');
        if (matrixParamPos >= 0) {
            path = path.substring(0, matrixParamPos);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private boolean isBoardAdminPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        // 배포 환경/프록시 설정에 따라 context 경로가 포함된 채 들어오는 경우를 함께 허용한다.
        return path.contains("/mgmt/board/")
                || path.endsWith("/mgmt/board")
                || path.contains("/mgmt/boards/")
                || path.endsWith("/mgmt/boards")
                || path.contains("/mgmt/boardgroup/")
                || path.endsWith("/mgmt/boardgroup")
                || path.contains("/mgmt/boardgroupmember/")
                || path.endsWith("/mgmt/boardgroupmember");
    }
}
