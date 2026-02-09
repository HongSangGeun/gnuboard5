package com.deepcode.springboard.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 모니터링 서비스 자동 체크 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringScheduler {

    private final MonitoringServiceManager monitoringServiceManager;
    private final Map<Integer, LocalDateTime> lastCheckTimes = new ConcurrentHashMap<>();

    /**
     * 1분마다 실행되는 모니터링 체크
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000) // 1분마다, 시작 후 10초 대기
    public void checkMonitoringServices() {
        List<MonitoringService> services = monitoringServiceManager.getEnabledServices();

        if (services.isEmpty()) {
            log.debug("No enabled monitoring services");
            return;
        }

        log.info("Running scheduled monitoring check for {} services", services.size());

        for (MonitoringService service : services) {
            try {
                if (shouldCheck(service)) {
                    log.info("Checking service: {} ({})", service.getMsName(), service.getMsUrl());
                    monitoringServiceManager.checkService(service);
                    lastCheckTimes.put(service.getMsId(), LocalDateTime.now());
                }
            } catch (Exception e) {
                log.error("Error checking service {}: {}", service.getMsName(), e.getMessage(), e);
            }
        }
    }

    /**
     * 서비스를 체크해야 하는지 확인
     */
    private boolean shouldCheck(MonitoringService service) {
        LocalDateTime lastCheck = lastCheckTimes.get(service.getMsId());

        if (lastCheck == null) {
            // 첫 체크
            return true;
        }

        int checkInterval = service.getMsCheckInterval() != null ? service.getMsCheckInterval() : 60;
        LocalDateTime nextCheckTime = lastCheck.plusSeconds(checkInterval);

        return LocalDateTime.now().isAfter(nextCheckTime);
    }

    /**
     * 오래된 모니터링 이력 정리 (매일 새벽 3시)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanOldHistory() {
        try {
            int deletedCount = monitoringServiceManager.cleanOldHistory(30); // 30일 이상 된 데이터 삭제
            log.info("Cleaned {} old monitoring history records", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning old monitoring history", e);
        }
    }
}
