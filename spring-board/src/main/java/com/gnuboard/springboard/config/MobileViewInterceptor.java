package com.gnuboard.springboard.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

public class MobileViewInterceptor implements HandlerInterceptor {
    public static final String ATTR_IS_MOBILE_VIEW = "isMobileView";
    private static final String SESSION_DEVICE_MODE = "device_mode";
    private final ResourceLoader resourceLoader;

    public MobileViewInterceptor(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(true);
        String requestedDevice = normalizeDevice(request.getParameter("device"));
        if (requestedDevice != null) {
            session.setAttribute(SESSION_DEVICE_MODE, requestedDevice);
        }

        String deviceMode = (String) session.getAttribute(SESSION_DEVICE_MODE);
        boolean isMobile = "mobile".equals(deviceMode) || ("auto".equals(deviceMode) || !StringUtils.hasText(deviceMode))
                && isMobileUserAgent(request.getHeader("User-Agent"));
        request.setAttribute(ATTR_IS_MOBILE_VIEW, isMobile);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView == null) {
            return;
        }
        String viewName = modelAndView.getViewName();
        if (!StringUtils.hasText(viewName)) {
            return;
        }
        if (viewName.startsWith("redirect:") || viewName.startsWith("forward:")) {
            return;
        }
        if (request.getRequestURI().startsWith(request.getContextPath() + "/mgmt")) {
            return;
        }
        if (!Boolean.TRUE.equals(request.getAttribute(ATTR_IS_MOBILE_VIEW))) {
            return;
        }

        String mobileViewName = "mobile/" + viewName;
        Resource resource = resourceLoader.getResource("classpath:/templates/" + mobileViewName + ".html");
        if (resource.exists()) {
            modelAndView.setViewName(mobileViewName);
        }
    }

    private String normalizeDevice(String device) {
        if (!StringUtils.hasText(device)) {
            return null;
        }
        String value = device.trim().toLowerCase(Locale.ROOT);
        if ("mobile".equals(value) || "pc".equals(value) || "auto".equals(value)) {
            return value;
        }
        return null;
    }

    private boolean isMobileUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return false;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        return ua.contains("android")
                || ua.contains("iphone")
                || ua.contains("ipad")
                || ua.contains("ipod")
                || ua.contains("windows phone")
                || ua.contains("blackberry")
                || ua.contains("opera mini")
                || ua.contains("opera mobi")
                || ua.contains("mobile");
    }
}
