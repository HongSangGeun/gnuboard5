package com.deepcode.springboard.member;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {
    private final MemberService memberService;

    public RegisterController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/register")
    public String registerForm(Model model, HttpServletRequest request) {
        model.addAttribute("form", new RegisterForm());
        if (Boolean.TRUE.equals(request.getAttribute("isMobileView")) || isMobileUserAgent(request.getHeader("User-Agent"))) {
            return "mobile/member/register";
        }
        return "member/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterForm form,
            RedirectAttributes redirectAttributes) {
        try {
            if (!form.getPassword().equals(form.getPasswordConfirm())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            memberService.register(
                    form.getMbId(),
                    form.getPassword(),
                    form.getName(),
                    form.getNick(),
                    form.getEmail());
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 관리자 승인 후 로그인 가능합니다.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
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
