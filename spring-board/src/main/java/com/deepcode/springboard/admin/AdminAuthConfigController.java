package com.deepcode.springboard.admin;

import com.deepcode.springboard.admin.service.AdminMenuService;
import com.deepcode.springboard.member.Member;
import com.deepcode.springboard.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mgmt/config/auth")
@RequiredArgsConstructor
public class AdminAuthConfigController {

    private final AuthService authService;
    private final AdminMenuService adminMenuService;
    private final MemberMapper memberMapper;

    @GetMapping
    public String index(Model model) {
        List<AuthMapper.AuthDTO> authList = authService.getAuthList();
        model.addAttribute("authList", authList);
        // Pass menu list for the dropdown
        model.addAttribute("menuList", adminMenuService.getAdminMenus());
        model.addAttribute("subMenu", "100200");
        model.addAttribute("pageTitle", "관리권한설정");
        return "admin/config/auth_list";
    }

    @PostMapping("/update")
    public String update(@RequestParam String mbId,
            @RequestParam String auMenu,
            @RequestParam(required = false) List<String> auths) {

        String authString = "";
        if (auths != null) {
            authString = String.join(",", auths);
        }

        Auth auth = new Auth();
        auth.setMbId(mbId);
        auth.setAuMenu(auMenu);
        auth.setAuAuth(authString);

        authService.addAuth(auth);

        return "redirect:/mgmt/config/auth";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam String mbId, @RequestParam String auMenu) {
        authService.deleteAuth(mbId, auMenu);
        return "redirect:/mgmt/config/auth";
    }

    @GetMapping("/search-members")
    @ResponseBody
    public List<Map<String, String>> searchMembers(@RequestParam String query) {
        if (query == null || query.length() < 1) {
            return List.of();
        }

        List<Member> members = memberMapper.searchByIdOrNick(query + "%", 10);
        return members.stream()
                .map(m -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", m.getMbId());
                    map.put("nick", m.getMbNick() != null ? m.getMbNick() : "");
                    map.put("name", m.getMbName() != null ? m.getMbName() : "");
                    return map;
                })
                .collect(Collectors.toList());
    }
}
