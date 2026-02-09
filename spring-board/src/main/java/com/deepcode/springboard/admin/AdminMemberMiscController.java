package com.deepcode.springboard.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/mgmt")
@RequiredArgsConstructor
public class AdminMemberMiscController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 접속자 집계
     */
    @GetMapping("/visit")
    public String visit(@RequestParam(defaultValue = "7") int days, Model model) {
        model.addAttribute("subMenu", "200800");
        model.addAttribute("pageTitle", "접속자집계");

        try {
            // 최근 N일간의 접속자 집계
            String sql = """
                SELECT vs_date, vs_count
                FROM g5_visit_sum
                WHERE vs_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                ORDER BY vs_date DESC
                """;
            List<Map<String, Object>> visitStats = jdbcTemplate.queryForList(sql, days);

            // 전체 접속자 수
            Integer totalVisit = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(vs_count), 0) FROM g5_visit_sum", Integer.class);

            // 오늘 접속자 수
            Integer todayVisit = jdbcTemplate.queryForObject(
                "SELECT COALESCE(vs_count, 0) FROM g5_visit_sum WHERE vs_date = CURDATE()", Integer.class);

            model.addAttribute("visitStats", visitStats);
            model.addAttribute("totalVisit", totalVisit);
            model.addAttribute("todayVisit", todayVisit);
            model.addAttribute("days", days);
        } catch (Exception e) {
            log.error("접속자 집계 조회 실패: {}", e.getMessage());
            model.addAttribute("visitStats", List.of());
            model.addAttribute("totalVisit", 0);
            model.addAttribute("todayVisit", 0);
        }

        return "admin/visit/stats";
    }

    /**
     * 접속자 검색
     */
    @GetMapping("/visit/search")
    public String visitSearch(@RequestParam(required = false) String searchDate,
                             @RequestParam(required = false) String searchIp,
                             @RequestParam(defaultValue = "1") int page,
                             Model model) {
        model.addAttribute("subMenu", "200810");
        model.addAttribute("pageTitle", "접속자검색");

        int pageSize = 50;
        int offset = (page - 1) * pageSize;

        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM g5_visit WHERE 1=1 ");

            if (searchDate != null && !searchDate.isEmpty()) {
                sql.append("AND DATE(vi_datetime) = '").append(searchDate).append("' ");
            }
            if (searchIp != null && !searchIp.isEmpty()) {
                sql.append("AND vi_ip LIKE '%").append(searchIp).append("%' ");
            }

            sql.append("ORDER BY vi_datetime DESC LIMIT ").append(pageSize).append(" OFFSET ").append(offset);

            List<Map<String, Object>> visitLogs = jdbcTemplate.queryForList(sql.toString());

            // 총 개수
            String countSql = "SELECT COUNT(*) FROM g5_visit WHERE 1=1 ";
            if (searchDate != null && !searchDate.isEmpty()) {
                countSql += "AND DATE(vi_datetime) = '" + searchDate + "' ";
            }
            if (searchIp != null && !searchIp.isEmpty()) {
                countSql += "AND vi_ip LIKE '%" + searchIp + "%' ";
            }
            Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);

            model.addAttribute("visitLogs", visitLogs);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) totalCount / pageSize));
            model.addAttribute("searchDate", searchDate);
            model.addAttribute("searchIp", searchIp);
        } catch (Exception e) {
            log.error("접속자 검색 실패: {}", e.getMessage());
            model.addAttribute("visitLogs", List.of());
            model.addAttribute("totalCount", 0);
        }

        return "admin/visit/search";
    }

    /**
     * 접속자 로그 삭제
     */
    @GetMapping("/visit/delete")
    public String visitDelete(Model model, HttpServletRequest request) {
        model.addAttribute("subMenu", "200820");
        model.addAttribute("pageTitle", "접속자로그삭제");
        model.addAttribute("csrfToken",
            com.deepcode.springboard.security.CsrfTokenManager.ensureToken(request.getSession()));
        return "admin/visit/delete";
    }

    @PostMapping("/visit/delete")
    public String visitDeleteProcess(@RequestParam String deleteType,
                                    @RequestParam(required = false) Integer days,
                                    @RequestParam(required = false) String beforeDate,
                                    @RequestParam String _csrf,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        // CSRF 검증
        String sessionToken = com.deepcode.springboard.security.CsrfTokenManager.ensureToken(request.getSession());
        if (!com.deepcode.springboard.security.CsrfTokenManager.matches(sessionToken, _csrf)) {
            redirectAttributes.addFlashAttribute("error", "잘못된 요청입니다.");
            return "redirect:/mgmt/visit/delete";
        }

        try {
            int deletedCount = 0;

            if ("days".equals(deleteType) && days != null) {
                // N일 이전 데이터 삭제
                String sql = "DELETE FROM g5_visit WHERE vi_datetime < DATE_SUB(NOW(), INTERVAL ? DAY)";
                deletedCount = jdbcTemplate.update(sql, days);
            } else if ("date".equals(deleteType) && beforeDate != null && !beforeDate.isEmpty()) {
                // 특정 날짜 이전 데이터 삭제
                String sql = "DELETE FROM g5_visit WHERE DATE(vi_datetime) < ?";
                deletedCount = jdbcTemplate.update(sql, beforeDate);
            } else if ("all".equals(deleteType)) {
                // 전체 삭제
                deletedCount = jdbcTemplate.update("DELETE FROM g5_visit");
            }

            redirectAttributes.addFlashAttribute("message",
                deletedCount + "건의 접속자 로그가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("접속자 로그 삭제 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/mgmt/visit/delete";
    }

    /**
     * 투표 관리
     */
    @GetMapping("/poll")
    public String poll(Model model) {
        model.addAttribute("subMenu", "200900");
        model.addAttribute("pageTitle", "투표관리");

        try {
            List<Map<String, Object>> polls = jdbcTemplate.queryForList(
                "SELECT * FROM g5_poll ORDER BY po_id DESC");
            model.addAttribute("polls", polls);
        } catch (Exception e) {
            log.error("투표 목록 조회 실패: {}", e.getMessage());
            model.addAttribute("polls", List.of());
        }

        return "admin/poll/list";
    }
}
