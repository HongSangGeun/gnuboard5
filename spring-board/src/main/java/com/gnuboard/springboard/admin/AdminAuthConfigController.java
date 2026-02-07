package com.gnuboard.springboard.admin;

import com.gnuboard.springboard.admin.service.AdminMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/mgmt/config/auth")
@RequiredArgsConstructor
public class AdminAuthConfigController {

    private final AuthService authService;
    private final AdminMenuService adminMenuService;

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
}
