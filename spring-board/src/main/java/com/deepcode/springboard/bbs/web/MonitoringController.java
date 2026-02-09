package com.deepcode.springboard.bbs.web;

import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.health.MonitoringHistory;
import com.deepcode.springboard.health.MonitoringService;
import com.deepcode.springboard.health.MonitoringServiceManager;
import com.deepcode.springboard.member.LoginUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * 일반 사용자용 모니터링 뷰 컨트롤러
 */
@Controller
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringServiceManager monitoringServiceManager;

    /**
     * 서비스 이력 조회 (로그인 필요)
     */
    @GetMapping("/history/{msId}")
    public String history(
            @PathVariable Integer msId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1000") int limit,
            HttpSession session,
            Model model) {

        // 로그인 체크
        LoginUser user = (LoginUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (user == null) {
            String url = URLEncoder.encode("/monitoring/history/" + msId, StandardCharsets.UTF_8);
            return "redirect:/login?url=" + url;
        }

        MonitoringService service = monitoringServiceManager.getService(msId);
        if (service == null) {
            return "redirect:/";
        }

        // 날짜 범위가 지정되지 않으면 기본값 설정 (최근 7일)
        if (startDate == null || startDate.isBlank()) {
            startDate = LocalDate.now().minusDays(7).toString() + " 00:00:00";
        } else {
            startDate = startDate + " 00:00:00";
        }

        if (endDate == null || endDate.isBlank()) {
            endDate = LocalDate.now().toString() + " 23:59:59";
        } else {
            endDate = endDate + " 23:59:59";
        }

        List<MonitoringHistory> history = monitoringServiceManager.getServiceHistoryByDateRange(
                msId, startDate, endDate, limit);

        model.addAttribute("service", service);
        model.addAttribute("history", history);
        model.addAttribute("startDate", startDate.substring(0, 10));
        model.addAttribute("endDate", endDate.substring(0, 10));
        model.addAttribute("pageTitle", service.getMsName() + " - 모니터링 이력");
        return "monitoring/history";
    }
}
