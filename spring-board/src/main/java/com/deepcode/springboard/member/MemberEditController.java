package com.deepcode.springboard.member;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.deepcode.springboard.common.SessionConst;

@Controller
public class MemberEditController {
    private final MemberService memberService;

    public MemberEditController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/member/edit")
    public String edit(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 한 회원만 접근하실 수 있습니다.");
            return "redirect:/login";
        }
        if (!Boolean.TRUE.equals(session.getAttribute("member_confirmed"))) {
            return "redirect:/member/confirm?url=/member/edit";
        }

        Member member = memberService.getMember(loginUser.getId());
        if (member == null) {
            redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
            return "redirect:/";
        }

        MemberEditForm form = new MemberEditForm();
        form.setMbId(member.getMbId());
        form.setName(member.getMbName());
        form.setNick(member.getMbNick());
        form.setEmail(member.getMbEmail());
        form.setHp(member.getMbHp());
        form.setTel(member.getMbTel());
        form.setAddr1(member.getMbAddr1());
        form.setAddr2(member.getMbAddr2());
        form.setAddr3(member.getMbAddr3());
        form.setZip1(member.getMbZip1());
        form.setZip2(member.getMbZip2());

        model.addAttribute("form", form);
        model.addAttribute("pageTitle", "회원정보 수정");
        model.addAttribute("loginUser", loginUser);
        if (isMobile(session)) {
            return "mobile/member/edit";
        }
        return "member/edit";
    }

    @PostMapping("/member/edit")
    public String editSubmit(@ModelAttribute("form") MemberEditForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 한 회원만 접근하실 수 있습니다.");
            return "redirect:/login";
        }
        if (!Boolean.TRUE.equals(session.getAttribute("member_confirmed"))) {
            return "redirect:/member/confirm?url=/member/edit";
        }

        if (StringUtils.hasText(form.getPassword())
                && !form.getPassword().equals(form.getPasswordConfirm())) {
            model.addAttribute("pageTitle", "회원정보 수정");
            model.addAttribute("loginUser", loginUser);
            if (isMobile(session)) {
                return "mobile/member/edit";
            }
            return "member/edit";
        }

        Member existing = memberService.getMember(loginUser.getId());
        if (existing == null) {
            model.addAttribute("pageTitle", "회원정보 수정");
            model.addAttribute("loginUser", loginUser);
            if (isMobile(session)) {
                return "mobile/member/edit";
            }
            return "member/edit";
        }

        Member member = new Member();
        member.setMbId(loginUser.getId());
        member.setMbName(form.getName());
        member.setMbNick(form.getNick());
        member.setMbEmail(form.getEmail());
        member.setMbHp(form.getHp());
        member.setMbTel(form.getTel());
        member.setMbAddr1(form.getAddr1());
        member.setMbAddr2(form.getAddr2());
        member.setMbAddr3(form.getAddr3());
        member.setMbZip1(form.getZip1());
        member.setMbZip2(form.getZip2());
        member.setMbPassword(StringUtils.hasText(form.getPassword()) ? form.getPassword() : null);
        member.setMbLevel(existing.getMbLevel());
        member.setMbAddrJibeon(existing.getMbAddrJibeon());
        member.setMbProfile(existing.getMbProfile());
        member.setMbMemo(existing.getMbMemo());
        member.setMbInterceptDate(existing.getMbInterceptDate());
        member.setMbLeaveDate(existing.getMbLeaveDate());

        memberService.updateMember(member);
        session.removeAttribute("member_confirmed");
        redirectAttributes.addFlashAttribute("message", "회원정보가 수정되었습니다.");
        return "redirect:/member/edit";
    }

    private boolean isMobile(HttpSession session) {
        org.springframework.web.context.request.RequestAttributes attrs = org.springframework.web.context.request.RequestContextHolder
                .getRequestAttributes();
        if (attrs != null) {
            Object mobile = attrs.getAttribute("isMobileView",
                    org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST);
            return Boolean.TRUE.equals(mobile);
        }
        return false;
    }
}
