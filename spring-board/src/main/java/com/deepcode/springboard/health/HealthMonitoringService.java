package com.deepcode.springboard.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스 모니터링 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthMonitoringService {

    private final HealthEndpoint healthEndpoint;
    private final DataSource dataSource;

    /**
     * 전체 시스템 헬스 체크
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            // Spring Boot Actuator의 헬스 정보 가져오기
            HealthComponent healthComponent = healthEndpoint.health();

            if (healthComponent instanceof SystemHealth) {
                SystemHealth systemHealth = (SystemHealth) healthComponent;
                healthInfo.put("status", systemHealth.getStatus().getCode());
                healthInfo.put("components", systemHealth.getComponents());
            } else {
                healthInfo.put("status", healthComponent.getStatus().getCode());
                healthInfo.put("components", new HashMap<>());
            }

        } catch (Exception e) {
            log.error("Error getting system health", e);
            healthInfo.put("status", "ERROR");
            healthInfo.put("error", e.getMessage());
        }

        return healthInfo;
    }

    /**
     * 데이터베이스 헬스 체크
     */
    public Map<String, Object> getDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            long startTime = System.currentTimeMillis();
            boolean isValid = connection.isValid(5);
            long responseTime = System.currentTimeMillis() - startTime;

            dbHealth.put("status", isValid ? "UP" : "DOWN");
            dbHealth.put("responseTime", responseTime + "ms");
            dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
            dbHealth.put("url", connection.getMetaData().getURL());

        } catch (Exception e) {
            log.error("Database health check failed", e);
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }

        return dbHealth;
    }

    /**
     * 시스템 리소스 정보
     */
    public Map<String, Object> getSystemResources() {
        Map<String, Object> resources = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        resources.put("maxMemory", formatBytes(maxMemory));
        resources.put("totalMemory", formatBytes(totalMemory));
        resources.put("freeMemory", formatBytes(freeMemory));
        resources.put("usedMemory", formatBytes(usedMemory));
        resources.put("memoryUsagePercent", (usedMemory * 100) / totalMemory);
        resources.put("availableProcessors", runtime.availableProcessors());

        return resources;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
