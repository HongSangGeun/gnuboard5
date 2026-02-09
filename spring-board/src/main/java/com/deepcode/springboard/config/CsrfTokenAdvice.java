package com.deepcode.springboard.config;

import com.deepcode.springboard.security.CsrfTokenManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CsrfTokenAdvice {
    @ModelAttribute
    public void addCsrfToken(HttpSession session, Model model) {
        model.addAttribute("csrfToken", CsrfTokenManager.ensureToken(session));
    }
}
