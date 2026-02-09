package com.deepcode.springboard.admin;

import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.config.G5ConfigService;
import com.deepcode.springboard.member.LoginUser;
import com.deepcode.springboard.member.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mgmt/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final ConfigService configService;
    private final G5ConfigService g5ConfigService;
    private final MemberService memberService;
    private final AdminSkinOptionService skinOptionService;

    @GetMapping
    public String index(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        Config config = configService.getConfig();
        model.addAttribute("config", config);
        model.addAttribute("adminMemberOptions", memberService.getMembersForSelect());
        model.addAttribute("latestSkinOptions", skinOptionService.listLatestSkins());
        model.addAttribute("subMenu", "100100");
        model.addAttribute("pageTitle", "기본환경설정");
        return "admin/config/form";
    }

    @PostMapping
    public String update(@ModelAttribute Config config, HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("DEBUG: Config Update Received: " + config);
        System.out.println("DEBUG: cfId=" + config.getCfId() + ", cfTitle=" + config.getCfTitle());
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        configService.updateConfig(config);
        return "redirect:/mgmt/config";
    }

    private boolean requireAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser user = loginUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return false;
        }
        if (!g5ConfigService.isAdmin(user)) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        return true;
    }

    private LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser) {
            return (LoginUser) value;
        }
        return null;
    }
}
