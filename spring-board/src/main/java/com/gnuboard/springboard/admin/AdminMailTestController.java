package com.gnuboard.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mgmt/config/mailtest")
@RequiredArgsConstructor
public class AdminMailTestController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("subMenu", "100300");
        model.addAttribute("pageTitle", "메일 테스트");
        return "admin/config/mailtest";
    }

    @PostMapping("/send")
    public String send(@RequestParam String email, Model model) {
        model.addAttribute("subMenu", "100300");
        model.addAttribute("pageTitle", "메일 테스트");

        // Mock sending or check for mail sender
        // In a real scenario, inject JavaMailSender and try-catch

        boolean mailConfigured = false; // Check application.yml properties or injected bean

        if (!mailConfigured) {
            model.addAttribute("error", "메일 서버 설정이 되어있지 않습니다. application.yml을 확인해주세요.");
        } else {
            // sendMail(email, ...);
            model.addAttribute("message", "메일이 발송되었습니다 (실제 발송은 설정 필요).");
        }

        return "admin/config/mailtest";
    }
}
