package com.deepcode.springboard.admin;

import com.deepcode.springboard.admin.service.DbUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DB 업그레이드 관리 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/mgmt/config/dbupgrade")
@RequiredArgsConstructor
public class AdminDbUpgradeController {

    private final DbUpgradeService dbUpgradeService;

    /**
     * DB 업그레이드 메인 페이지
     */
    @GetMapping
    public String index(Model model) {
        try {
            // 이력 테이블이 없으면 먼저 생성
            dbUpgradeService.createUpgradeHistoryTable();

            // DB 상태 확인
            Map<String, Object> status = dbUpgradeService.checkDatabaseStatus();
            model.addAttribute("status", status);

            // 최근 업그레이드 이력
            List<Map<String, Object>> history = dbUpgradeService.getUpgradeHistory(20);
            model.addAttribute("history", history);

        } catch (Exception e) {
            log.error("Error loading DB upgrade page", e);
            model.addAttribute("error", "DB 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
        }

        model.addAttribute("subMenu", "100410");
        model.addAttribute("pageTitle", "DB 업그레이드");
        return "admin/config/dbupgrade";
    }

    /**
     * DB 업그레이드 실행 (테이블 생성/컬럼 추가)
     */
    @PostMapping("/execute")
    @ResponseBody
    public Map<String, Object> executeUpgrade() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<String> results = dbUpgradeService.executeUpgrade();

            response.put("success", true);
            response.put("results", results);
            response.put("message", "DB 업그레이드가 완료되었습니다.");

        } catch (Exception e) {
            log.error("Error executing DB upgrade", e);
            response.put("success", false);
            response.put("message", "DB 업그레이드 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }

    /**
     * 업그레이드 스크립트 실행 (스키마 변경)
     */
    @PostMapping("/execute-scripts")
    @ResponseBody
    public Map<String, Object> executeUpgradeScripts() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<String> results = dbUpgradeService.executeUpgradeScripts();

            response.put("success", true);
            response.put("results", results);
            response.put("message", "업그레이드 스크립트가 완료되었습니다.");

        } catch (Exception e) {
            log.error("Error executing upgrade scripts", e);
            response.put("success", false);
            response.put("message", "업그레이드 스크립트 실행 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }

    /**
     * DB 상태 확인
     */
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> checkStatus() {
        try {
            return dbUpgradeService.checkDatabaseStatus();
        } catch (Exception e) {
            log.error("Error checking DB status", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
}
