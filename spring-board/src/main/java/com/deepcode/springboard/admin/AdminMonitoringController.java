package com.deepcode.springboard.admin;

import com.deepcode.springboard.health.MonitoringHistory;
import com.deepcode.springboard.health.MonitoringService;
import com.deepcode.springboard.health.MonitoringServiceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 외부 서비스 모니터링 관리 컨트롤러
 */
@Controller
@RequestMapping("/mgmt/monitoring")
@RequiredArgsConstructor
public class AdminMonitoringController {

    private final MonitoringServiceManager monitoringServiceManager;

    /**
     * 모니터링 서비스 목록
     */
    @GetMapping
    public String list(Model model) {
        List<MonitoringService> services = monitoringServiceManager.getAllServices();
        model.addAttribute("services", services);
        model.addAttribute("subMenu", "100260");
        model.addAttribute("pageTitle", "외부 서비스 모니터링");
        return "admin/monitoring/list";
    }

    /**
     * 서비스 등록 폼
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("subMenu", "100260");
        model.addAttribute("pageTitle", "서비스 등록");
        return "admin/monitoring/form";
    }

    /**
     * 서비스 수정 폼
     */
    @GetMapping("/edit/{msId}")
    public String editForm(@PathVariable Integer msId, Model model) {
        MonitoringService service = monitoringServiceManager.getService(msId);
        model.addAttribute("service", service);
        model.addAttribute("subMenu", "100260");
        model.addAttribute("pageTitle", "서비스 수정");
        return "admin/monitoring/form";
    }

    /**
     * 서비스 저장
     */
    @PostMapping("/save")
    public String save(MonitoringService service) {
        if (service.getMsId() == null) {
            monitoringServiceManager.createService(service);
        } else {
            monitoringServiceManager.updateService(service);
        }
        return "redirect:/mgmt/monitoring";
    }

    /**
     * 서비스 삭제
     */
    @PostMapping("/delete/{msId}")
    public String delete(@PathVariable Integer msId) {
        monitoringServiceManager.deleteService(msId);
        return "redirect:/mgmt/monitoring";
    }

    /**
     * 서비스 즉시 체크
     */
    @PostMapping("/check/{msId}")
    @ResponseBody
    public Map<String, Object> checkService(@PathVariable Integer msId) {
        MonitoringService service = monitoringServiceManager.getService(msId);
        return monitoringServiceManager.checkService(service);
    }

    /**
     * 서비스 이력 조회
     */
    @GetMapping("/history/{msId}")
    public String history(
            @PathVariable Integer msId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1000") int limit,
            Model model) {

        MonitoringService service = monitoringServiceManager.getService(msId);

        // 날짜 범위가 지정되지 않으면 기본값 설정 (최근 7일)
        if (startDate == null || startDate.isBlank()) {
            startDate = java.time.LocalDate.now().minusDays(7).toString() + " 00:00:00";
        } else {
            startDate = startDate + " 00:00:00";
        }

        if (endDate == null || endDate.isBlank()) {
            endDate = java.time.LocalDate.now().toString() + " 23:59:59";
        } else {
            endDate = endDate + " 23:59:59";
        }

        List<MonitoringHistory> history = monitoringServiceManager.getServiceHistoryByDateRange(
                msId, startDate, endDate, limit);

        model.addAttribute("service", service);
        model.addAttribute("history", history);
        model.addAttribute("startDate", startDate.substring(0, 10));
        model.addAttribute("endDate", endDate.substring(0, 10));
        model.addAttribute("subMenu", "100260");
        model.addAttribute("pageTitle", service.getMsName() + " - 모니터링 이력");
        return "admin/monitoring/history";
    }

    /**
     * 모든 서비스 상태 API
     */
    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Map<String, Object>> getAllStatus() {
        return monitoringServiceManager.checkAllEnabledServices();
    }
}
