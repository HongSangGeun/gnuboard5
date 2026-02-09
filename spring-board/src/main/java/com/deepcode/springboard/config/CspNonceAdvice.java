package com.deepcode.springboard.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * CSP nonce를 모든 Thymeleaf 템플릿에 제공하는 ControllerAdvice.
 * CspFilter에서 요청마다 생성한 nonce를 ${cspNonce} 변수로 노출합니다.
 */
@ControllerAdvice
public class CspNonceAdvice {
    @ModelAttribute
    public void addCspNonce(HttpServletRequest request, Model model) {
        Object nonce = request.getAttribute("cspNonce");
        if (nonce != null) {
            model.addAttribute("cspNonce", nonce);
        }
    }
}
