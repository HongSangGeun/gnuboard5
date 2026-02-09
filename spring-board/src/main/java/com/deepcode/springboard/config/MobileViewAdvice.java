package com.deepcode.springboard.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class MobileViewAdvice {

    @ModelAttribute("isMobileView")
    public boolean isMobileView(HttpServletRequest request) {
        Object value = request.getAttribute(MobileViewInterceptor.ATTR_IS_MOBILE_VIEW);
        return Boolean.TRUE.equals(value);
    }
}
