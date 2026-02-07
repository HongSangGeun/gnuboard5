package com.gnuboard.springboard.install;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InstallController {

    private final InstallService installService;

    public InstallController(InstallService installService) {
        this.installService = installService;
    }

    @GetMapping("/install")
    public String installForm(Model model) {
        model.addAttribute("form", new InstallForm());
        return "install/install";
    }

    @PostMapping("/install")
    public String install(@ModelAttribute InstallForm form, RedirectAttributes redirectAttributes) {
        try {
            installService.install(form);
            redirectAttributes.addFlashAttribute("message", "설치가 성공적으로 완료되었습니다. 서버를 재시작해주세요.");
            return "redirect:/login"; // Or intermediate completion page
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "설치 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/install";
        }
    }
}
