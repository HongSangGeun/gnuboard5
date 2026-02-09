package com.deepcode.springboard.admin;

import com.deepcode.springboard.health.HealthMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 헬스 모니터링 관리자 컨트롤러
 */
@Controller
@RequestMapping("/mgmt/health")
@RequiredArgsConstructor
public class AdminHealthController {

    private final HealthMonitoringService healthMonitoringService;

    /**
     * 헬스 모니터링 대시보드
     */
    @GetMapping
    public String healthDashboard(Model model) {
        model.addAttribute("subMenu", "100250");
        model.addAttribute("pageTitle", "시스템 모니터링");
        return "admin/health/dashboard";
    }

    /**
     * 헬스 정보 API (JSON)
     */
    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> getHealthInfo() {
        Map<String, Object> response = new HashMap<>();

        response.put("system", healthMonitoringService.getSystemHealth());
        response.put("database", healthMonitoringService.getDatabaseHealth());
        response.put("resources", healthMonitoringService.getSystemResources());
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }
}
