package com.gnuboard.springboard.member;

import com.gnuboard.springboard.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
    private final MemberService memberService;

    public LoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request) {
        if (Boolean.TRUE.equals(request.getAttribute("isMobileView"))
                || isMobileUserAgent(request.getHeader("User-Agent"))) {
            return "mobile/member/login";
        }
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String mbId,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        LoginUser user;
        try {
            user = memberService.authenticate(mbId, password);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/login";
        }
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
        session.setAttribute(SessionConst.LOGIN_MEMBER, user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SessionConst.LOGIN_MEMBER);
        return "redirect:/";
    }

    private boolean isMobileUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return false;
        }
        String ua = userAgent.toLowerCase();
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
