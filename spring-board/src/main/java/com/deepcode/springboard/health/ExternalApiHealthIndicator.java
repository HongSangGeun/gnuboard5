package com.deepcode.springboard.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 API 서비스 헬스체크
 */
@Slf4j
@Component("externalApi")
@RequiredArgsConstructor
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final MonitoringServiceManager monitoringServiceManager;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean allHealthy = true;

        try {
            List<MonitoringService> services = monitoringServiceManager.getEnabledServices();

            if (services.isEmpty()) {
                details.put("message", "No external services configured for monitoring");
                return Health.up().withDetails(details).build();
            }

            for (MonitoringService service : services) {
                Map<String, Object> result = monitoringServiceManager.checkService(service);
                details.put(service.getMsName(), result);

                if (!(Boolean) result.getOrDefault("healthy", false)) {
                    allHealthy = false;
                }
            }

        } catch (Exception e) {
            log.error("External API health check failed", e);
            details.put("error", e.getMessage());
            return Health.down().withDetails(details).build();
        }

        if (allHealthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
}
