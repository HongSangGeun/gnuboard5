package com.gnuboard.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/mgmt/config/theme")
@RequiredArgsConstructor
public class AdminThemeController {

    private final ConfigService configService;

    @GetMapping
    public String index(Model model) {
        List<ThemeInfo> themes = configService.getThemes();
        Config config = configService.getConfig();

        model.addAttribute("themes", themes);
        model.addAttribute("config", config);
        model.addAttribute("subMenu", "100280");
        model.addAttribute("pageTitle", "테마설정");

        return "admin/config/theme_list";
    }

    @PostMapping("/update")
    public String update(@RequestParam String theme) {
        configService.updateTheme(theme);
        return "redirect:/mgmt/config/theme";
    }

    @PostMapping("/reset")
    public String reset() {
        configService.updateTheme("");
        return "redirect:/mgmt/config/theme";
    }
}
