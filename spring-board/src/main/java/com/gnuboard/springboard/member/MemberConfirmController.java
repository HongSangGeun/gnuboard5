package com.gnuboard.springboard.member;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gnuboard.springboard.common.SessionConst;

@Controller
public class MemberConfirmController {
    private final MemberService memberService;

    public MemberConfirmController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/member/confirm")
    public String confirm(@RequestParam(required = false) String url,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 한 회원만 접근하실 수 있습니다.");
            return "redirect:/login";
        }
        String target = normalizeTarget(url);
        MemberConfirmForm form = new MemberConfirmForm();
        form.setUrl(target);
        model.addAttribute("form", form);
        model.addAttribute("pageTitle", "회원 비밀번호 확인");
        model.addAttribute("loginUser", loginUser);
        return "member/confirm";
    }

    @PostMapping("/member/confirm")
    public String confirmSubmit(@ModelAttribute("form") MemberConfirmForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 한 회원만 접근하실 수 있습니다.");
            return "redirect:/login";
        }

        if (!StringUtils.hasText(form.getPassword())) {
            model.addAttribute("error", "비밀번호를 입력해 주세요.");
            model.addAttribute("pageTitle", "회원 비밀번호 확인");
            model.addAttribute("loginUser", loginUser);
            return "member/confirm";
        }

        boolean ok = memberService.checkPassword(loginUser.getId(), form.getPassword());
        if (!ok) {
            model.addAttribute("error", "비밀번호가 올바르지 않습니다.");
            model.addAttribute("pageTitle", "회원 비밀번호 확인");
            model.addAttribute("loginUser", loginUser);
            return "member/confirm";
        }

        session.setAttribute("member_confirmed", Boolean.TRUE);
        return "redirect:" + normalizeTarget(form.getUrl());
    }

    private String normalizeTarget(String url) {
        if (!StringUtils.hasText(url)) {
            return "/member/edit";
        }
        String trimmed = url.trim();
        if (trimmed.contains("member_leave")) {
            return "/member/leave";
        }
        if (trimmed.contains("register_form")) {
            return "/member/edit";
        }
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.startsWith("//") || trimmed.contains("://")) {
            return "/member/edit";
        }
        return trimmed;
    }
}
