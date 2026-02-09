package com.deepcode.springboard.member;

import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.config.G5ConfigService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LoginAdvice {
    private final G5ConfigService configService;

    public LoginAdvice(G5ConfigService configService) {
        this.configService = configService;
    }

    @ModelAttribute("loginUser")
    public LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser) {
            return (LoginUser) value;
        }
        return null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser) {
            return configService.isAdmin((LoginUser) value);
        }
        return false;
    }
}
