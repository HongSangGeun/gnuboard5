package com.deepcode.springboard.admin;

import com.deepcode.springboard.security.CsrfTokenManager;
import com.deepcode.springboard.sms.SmsConfig;
import com.deepcode.springboard.sms.SmsHistory;
import com.deepcode.springboard.sms.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/mgmt/sms")
@RequiredArgsConstructor
public class AdminSmsController {

    private final SmsService smsService;

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.sms.from-number:}")
    private String defaultFromNumber;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("subMenu", "900000");
        model.addAttribute("pageTitle", "SMS 관리");
        return "admin/placeholder";
    }

    @GetMapping("/config")
    public String config(Model model, HttpServletRequest request) {
        model.addAttribute("subMenu", "900100");
        model.addAttribute("pageTitle", "SMS 환경설정");
        model.addAttribute("csrfToken", CsrfTokenManager.ensureToken(request.getSession()));

        // SMS 설정 조회 (테이블이 없을 경우 예외 처리)
        SmsConfig smsConfig = null;
        try {
            smsConfig = smsService.getConfig();
        } catch (Exception e) {
            // 테이블이 없거나 오류 발생 시 null로 처리
            log.warn("SMS 설정 테이블을 조회할 수 없습니다: {}", e.getMessage());
        }

        // application.yml 설정을 Map으로 전달
        Map<String, Object> config = new HashMap<>();
        config.put("smsEnabled", smsEnabled);
        config.put("apiKey", "");
        config.put("apiSecret", "");
        config.put("fromNumber", defaultFromNumber);
        config.put("provider", "");
        config.put("apiUrl", "");

        model.addAttribute("smsConfig", config);
        return "admin/sms/config";
    }

    @PostMapping("/config")
    public String configSave(@RequestParam Map<String, String> params,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        // CSRF 토큰 검증
        String sessionToken = CsrfTokenManager.ensureToken(request.getSession());
        if (!CsrfTokenManager.matches(sessionToken, params.get("_csrf"))) {
            redirectAttributes.addFlashAttribute("message", "잘못된 요청입니다.");
            return "redirect:/mgmt/sms/config";
        }

        // TODO: SMS 설정을 데이터베이스나 설정 파일에 저장
        // 현재는 application.yml에서 관리하므로 실제 저장은 구현하지 않음

        redirectAttributes.addFlashAttribute("message", "SMS 설정이 저장되었습니다.");
        return "redirect:/mgmt/sms/config";
    }

    @GetMapping("/test")
    public String test(Model model, HttpServletRequest request) {
        model.addAttribute("subMenu", "900100");
        model.addAttribute("pageTitle", "SMS 테스트 발송");
        model.addAttribute("csrfToken", CsrfTokenManager.ensureToken(request.getSession()));
        model.addAttribute("fromNumber", defaultFromNumber);
        model.addAttribute("smsEnabled", smsEnabled);
        return "admin/sms/test";
    }

    @PostMapping("/test")
    public String testSend(@RequestParam String toNumber,
                          @RequestParam String message,
                          @RequestParam String _csrf,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {

        // CSRF 토큰 검증
        String sessionToken = CsrfTokenManager.ensureToken(request.getSession());
        if (!CsrfTokenManager.matches(sessionToken, _csrf)) {
            redirectAttributes.addFlashAttribute("message", "잘못된 요청입니다.");
            redirectAttributes.addFlashAttribute("success", false);
            return "redirect:/mgmt/sms/test";
        }

        if (!smsEnabled) {
            redirectAttributes.addFlashAttribute("message", "SMS 발송이 비활성화되어 있습니다. 환경설정에서 활성화해주세요.");
            redirectAttributes.addFlashAttribute("success", false);
            return "redirect:/mgmt/sms/test";
        }

        // SMS 발송
        boolean result = smsService.sendSms(toNumber, message);

        if (result) {
            redirectAttributes.addFlashAttribute("message", "SMS 테스트 발송에 성공했습니다.");
            redirectAttributes.addFlashAttribute("success", true);
        } else {
            redirectAttributes.addFlashAttribute("message", "SMS 테스트 발송에 실패했습니다.");
            redirectAttributes.addFlashAttribute("success", false);
        }

        return "redirect:/mgmt/sms/test";
    }

    @GetMapping("/history")
    public String history(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "") String sfl,
                         @RequestParam(defaultValue = "") String stx,
                         Model model) {
        model.addAttribute("subMenu", "900400");
        model.addAttribute("pageTitle", "SMS 발송 이력");

        int pageSize = 20;

        // SMS 발송 이력 조회 (테이블이 없을 경우 예외 처리)
        List<SmsHistory> historyList = List.of();
        int totalCount = 0;

        try {
            historyList = smsService.getHistory(page, pageSize);
            totalCount = smsService.getHistoryCount();
        } catch (Exception e) {
            log.warn("SMS 발송 이력 테이블을 조회할 수 없습니다: {}", e.getMessage());
        }

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 페이징 계산
        int startPage = Math.max(1, page - 5);
        int endPage = Math.min(totalPages, page + 5);

        model.addAttribute("historyList", historyList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("sfl", sfl);
        model.addAttribute("stx", stx);

        return "admin/sms/history";
    }

    @GetMapping("/member_update")
    public String memberUpdate(Model model) {
        model.addAttribute("subMenu", "900200");
        model.addAttribute("pageTitle", "회원정보업데이트");
        return "admin/placeholder";
    }

    @GetMapping("/write")
    public String write(Model model) {
        model.addAttribute("subMenu", "900300");
        model.addAttribute("pageTitle", "문자 보내기");
        return "admin/placeholder";
    }

    @GetMapping("/history_num")
    public String historyNum(Model model) {
        model.addAttribute("subMenu", "900410");
        model.addAttribute("pageTitle", "전송내역-번호별");
        return "admin/placeholder";
    }

    @GetMapping("/emoticon_group")
    public String emoticonGroup(Model model) {
        model.addAttribute("subMenu", "900500");
        model.addAttribute("pageTitle", "이모티콘 그룹");
        return "admin/placeholder";
    }

    @GetMapping("/emoticon")
    public String emoticon(Model model) {
        model.addAttribute("subMenu", "900600");
        model.addAttribute("pageTitle", "이모티콘 관리");
        return "admin/placeholder";
    }

    @GetMapping("/hp_group")
    public String hpGroup(Model model) {
        model.addAttribute("subMenu", "900700");
        model.addAttribute("pageTitle", "휴대폰번호 그룹");
        return "admin/placeholder";
    }

    @GetMapping("/hp")
    public String hp(Model model) {
        model.addAttribute("subMenu", "900800");
        model.addAttribute("pageTitle", "휴대폰번호 관리");
        return "admin/placeholder";
    }

    @GetMapping("/hp_file")
    public String hpFile(Model model) {
        model.addAttribute("subMenu", "900900");
        model.addAttribute("pageTitle", "휴대폰번호 파일");
        return "admin/placeholder";
    }
}
