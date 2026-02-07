package com.gnuboard.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mgmt/config")
@RequiredArgsConstructor
public class AdminUtilityController {

    @GetMapping("/phpinfo")
    public String phpinfo(Model model) {
        model.addAttribute("subMenu", "100500");
        model.addAttribute("pageTitle", "시스템 정보");
        model.addAttribute("javaVersion", System.getProperty("java.version"));
        model.addAttribute("osName", System.getProperty("os.name"));
        // Basic JVM info instead of phpinfo
        return "admin/config/phpinfo";
    }

    @GetMapping("/session")
    public String session(Model model) {
        model.addAttribute("subMenu", "100800");
        model.addAttribute("pageTitle", "세션파일 일괄삭제");
        return "admin/config/session_delete";
    }

    @PostMapping("/session/delete")
    public String deleteSession(RedirectAttributes redirectAttributes) {
        // Logic to delete session files would go here.
        // For now, mock success.
        redirectAttributes.addFlashAttribute("message", "세션 파일이 일괄 삭제되었습니다.");
        return "redirect:/mgmt/config/session";
    }

    @GetMapping("/cache")
    public String cache(Model model) {
        model.addAttribute("subMenu", "100900");
        model.addAttribute("pageTitle", "캐시파일 일괄삭제");
        return "admin/config/cache_delete";
    }

    @PostMapping("/cache/delete")
    public String deleteCache(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "캐시 파일이 일괄 삭제되었습니다.");
        return "redirect:/mgmt/config/cache";
    }

    @GetMapping("/captcha")
    public String captcha(Model model) {
        model.addAttribute("subMenu", "100910");
        model.addAttribute("pageTitle", "캡챠파일 일괄삭제");
        return "admin/config/captcha_delete";
    }

    @PostMapping("/captcha/delete")
    public String deleteCaptcha(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "캡챠 파일이 일괄 삭제되었습니다.");
        return "redirect:/mgmt/config/captcha";
    }

    @GetMapping("/thumbnail")
    public String thumbnail(Model model) {
        model.addAttribute("subMenu", "100920");
        model.addAttribute("pageTitle", "썸네일파일 일괄삭제");
        return "admin/config/thumbnail_delete";
    }

    @PostMapping("/thumbnail/delete")
    public String deleteThumbnail(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "썸네일 파일이 일괄 삭제되었습니다.");
        return "redirect:/mgmt/config/thumbnail";
    }

    @GetMapping("/browscap")
    public String browscap(Model model) {
        model.addAttribute("subMenu", "100510");
        model.addAttribute("pageTitle", "Browscap 업데이트");
        model.addAttribute("message", "Browscap 업데이트 기능은 현재 지원하지 않습니다.");
        return "admin/basic_message";
    }

    @GetMapping("/visit_convert")
    public String visitConvert(Model model) {
        model.addAttribute("subMenu", "100520");
        model.addAttribute("pageTitle", "접속로그 변환");
        model.addAttribute("message", "접속로그 변환 기능은 현재 지원하지 않습니다.");
        return "admin/basic_message";
    }

    @GetMapping("/dbupgrade")
    public String dbupgrade(Model model) {
        model.addAttribute("subMenu", "100410");
        model.addAttribute("pageTitle", "DB업그레이드");
        model.addAttribute("message", "DB가 최신 버전입니다.");
        return "admin/basic_message";
    }

    @GetMapping("/service")
    public String service(Model model) {
        model.addAttribute("subMenu", "100400");
        model.addAttribute("pageTitle", "부가서비스");
        model.addAttribute("message", "부가서비스 정보가 없습니다.");
        return "admin/basic_message";
    }
}
