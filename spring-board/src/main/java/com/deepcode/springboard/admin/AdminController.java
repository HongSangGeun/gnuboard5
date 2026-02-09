package com.deepcode.springboard.admin;

import com.deepcode.springboard.admin.service.AdminMenuService;
import com.deepcode.springboard.health.MonitoringHistory;
import com.deepcode.springboard.health.MonitoringService;
import com.deepcode.springboard.health.MonitoringServiceManager;
import com.deepcode.springboard.member.Member;
import com.deepcode.springboard.member.MemberService;
import com.deepcode.springboard.member.SearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/mgmt")
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;
    private final MonitoringServiceManager monitoringServiceManager;
    private final AdminMenuService adminMenuService;

    @GetMapping
    public String index(Model model) {
        log.info("Loading admin index page");

        SearchCriteria criteria = new SearchCriteria();
        criteria.setSst("mb_datetime");
        criteria.setSod("desc");
        criteria.setPage(1);
        criteria.setLimit(5);
        criteria.setOffset(0);

        List<Member> newMembers = memberService.getMemberList(criteria);
        int memberCount = memberService.getMemberCount(criteria);

        model.addAttribute("newMembers", newMembers);
        model.addAttribute("memberCount", memberCount);

        // 모니터링 서비스 목록
        List<MonitoringService> monitoringServices = monitoringServiceManager.getEnabledServices();
        log.info("Found {} enabled monitoring services", monitoringServices.size());
        model.addAttribute("monitoringServices", monitoringServices);

        // 관리자 메뉴 추가
        model.addAttribute("adminMenus", adminMenuService.getAdminMenus());
        model.addAttribute("subMenu", "100000");

        return "admin/index";
    }

    /**
     * 모니터링 이력 데이터 API (차트용)
     */
    @GetMapping("/api/monitoring-chart-data")
    @ResponseBody
    public Map<String, Object> getMonitoringChartData() {
        log.info("API: Getting monitoring chart data");
        Map<String, Object> result = new HashMap<>();

        try {
            // 최근 24시간 이력 조회
            List<MonitoringHistory> recentHistory = monitoringServiceManager.getRecentHistory(24);
            log.info("Found {} history records in last 24 hours", recentHistory.size());

            // 서비스별로 그룹화
            Map<Integer, List<MonitoringHistory>> historyByService = recentHistory.stream()
                    .collect(Collectors.groupingBy(MonitoringHistory::getMsId));

            // 각 서비스별 데이터 생성
            List<Map<String, Object>> servicesData = new ArrayList<>();

            for (Map.Entry<Integer, List<MonitoringHistory>> entry : historyByService.entrySet()) {
                Integer msId = entry.getKey();
                List<MonitoringHistory> histories = entry.getValue();

                MonitoringService service = monitoringServiceManager.getService(msId);
                if (service == null) continue;

                // 최근 20개만 선택 (시간 역순으로 정렬 후)
                List<MonitoringHistory> recentHistories = histories.stream()
                        .sorted(Comparator.comparing(MonitoringHistory::getMhCheckedAt).reversed())
                        .limit(20)
                        .sorted(Comparator.comparing(MonitoringHistory::getMhCheckedAt)) // 시간순으로 재정렬
                        .collect(Collectors.toList());

                Map<String, Object> serviceData = new HashMap<>();
                serviceData.put("id", msId);
                serviceData.put("name", service.getMsName());
                serviceData.put("url", service.getMsUrl());

                // 시간 레이블 생성
                List<String> labels = recentHistories.stream()
                        .map(h -> h.getMhCheckedAt().toString().substring(11, 16)) // HH:mm 형식
                        .collect(Collectors.toList());

                // 응답시간 데이터
                List<Integer> responseTimes = recentHistories.stream()
                        .map(h -> h.getMhResponseTime() != null ? h.getMhResponseTime() : 0)
                        .collect(Collectors.toList());

                // 상태 데이터 (UP=1, DOWN/ERROR=0)
                List<Integer> statuses = recentHistories.stream()
                        .map(h -> "UP".equals(h.getMhStatus()) ? 1 : 0)
                        .collect(Collectors.toList());

                // 통계
                long upCount = recentHistories.stream()
                        .filter(h -> "UP".equals(h.getMhStatus()))
                        .count();
                double upRate = recentHistories.isEmpty() ? 0 : (upCount * 100.0 / recentHistories.size());

                Integer avgResponseTime = (int) responseTimes.stream()
                        .filter(t -> t > 0)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

                serviceData.put("labels", labels);
                serviceData.put("responseTimes", responseTimes);
                serviceData.put("statuses", statuses);
                serviceData.put("upRate", Math.round(upRate * 10) / 10.0);
                serviceData.put("avgResponseTime", avgResponseTime);
                serviceData.put("totalChecks", recentHistories.size());

                servicesData.add(serviceData);
            }

            result.put("success", true);
            result.put("services", servicesData);
            log.info("Returning {} services data", servicesData.size());

        } catch (Exception e) {
            log.error("Error getting monitoring chart data", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
