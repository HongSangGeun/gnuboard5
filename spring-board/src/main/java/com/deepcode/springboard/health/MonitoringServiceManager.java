package com.deepcode.springboard.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 서비스 모니터링 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringServiceManager {

    private final MonitoringMapper monitoringMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 모든 모니터링 서비스 조회
     */
    public List<MonitoringService> getAllServices() {
        return monitoringMapper.findAllServices();
    }

    /**
     * 활성화된 모니터링 서비스 조회
     */
    public List<MonitoringService> getEnabledServices() {
        return monitoringMapper.findEnabledServices();
    }

    /**
     * 서비스 조회
     */
    public MonitoringService getService(Integer msId) {
        return monitoringMapper.findServiceById(msId);
    }

    /**
     * 서비스 등록
     */
    public void createService(MonitoringService service) {
        if (service.getMsMethod() == null) service.setMsMethod("GET");
        if (service.getMsAuthType() == null) service.setMsAuthType("NONE");
        if (service.getMsTimeout() == null) service.setMsTimeout(5000);
        if (service.getMsExpectedStatus() == null) service.setMsExpectedStatus(200);
        if (service.getMsCheckInterval() == null) service.setMsCheckInterval(60);
        if (service.getMsEnabled() == null) service.setMsEnabled(true);
        if (service.getMsAlertEnabled() == null) service.setMsAlertEnabled(false);

        monitoringMapper.insertService(service);
        log.info("Monitoring service created: {}", service.getMsName());
    }

    /**
     * 서비스 수정
     */
    public void updateService(MonitoringService service) {
        monitoringMapper.updateService(service);
        log.info("Monitoring service updated: {}", service.getMsName());
    }

    /**
     * 서비스 삭제
     */
    public void deleteService(Integer msId) {
        monitoringMapper.deleteService(msId);
        log.info("Monitoring service deleted: {}", msId);
    }

    /**
     * 서비스 헬스 체크 수행
     */
    public Map<String, Object> checkService(MonitoringService service) {
        Map<String, Object> result = new HashMap<>();
        MonitoringHistory history = new MonitoringHistory();
        history.setMsId(service.getMsId());

        // CPU 사용량 캡처
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuUsage = osBean.getSystemCpuLoad() * 100; // 백분율로 변환
            if (cpuUsage >= 0) { // -1이 아닌 경우에만 저장
                history.setMhCpuUsage(Math.round(cpuUsage * 10.0) / 10.0); // 소수점 1자리
            }
        } catch (Exception e) {
            log.debug("Failed to get CPU usage: {}", e.getMessage());
        }

        try {
            // SSRF 방어: 내부 네트워크 접근 차단
            validateUrl(service.getMsUrl());

            long startTime = System.currentTimeMillis();

            // 인증 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            String authType = service.getMsAuthType() != null ? service.getMsAuthType() : "NONE";

            switch (authType) {
                case "BASIC":
                    if (service.getMsAuthUsername() != null && service.getMsAuthPassword() != null) {
                        String auth = service.getMsAuthUsername() + ":" + service.getMsAuthPassword();
                        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                        String authHeader = "Basic " + new String(encodedAuth);
                        headers.set("Authorization", authHeader);
                    }
                    break;
                case "BEARER":
                    if (service.getMsAuthToken() != null) {
                        headers.set("Authorization", "Bearer " + service.getMsAuthToken());
                    }
                    break;
                case "HEADER":
                    if (service.getMsAuthHeaderName() != null && service.getMsAuthHeaderValue() != null) {
                        headers.set(service.getMsAuthHeaderName(), service.getMsAuthHeaderValue());
                    }
                    break;
                case "NONE":
                default:
                    // 인증 없음
                    break;
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    service.getMsUrl(),
                    HttpMethod.valueOf(service.getMsMethod()),
                    entity,
                    String.class
            );

            long responseTime = System.currentTimeMillis() - startTime;

            HttpStatus statusCode = (HttpStatus) response.getStatusCode();
            boolean isHealthy = statusCode.value() == service.getMsExpectedStatus();

            // 응답 내용 검증
            if (isHealthy && service.getMsExpectedResponse() != null && !service.getMsExpectedResponse().isEmpty()) {
                String body = response.getBody();
                isHealthy = body != null && body.contains(service.getMsExpectedResponse());
            }

            history.setMhStatus(isHealthy ? "UP" : "DOWN");
            history.setMhStatusCode(statusCode.value());
            history.setMhResponseTime((int) responseTime);

            result.put("status", isHealthy ? "UP" : "DOWN");
            result.put("statusCode", statusCode.value());
            result.put("responseTime", responseTime + "ms");
            result.put("healthy", isHealthy);

        } catch (Exception e) {
            log.error("Health check failed for {}: {}", service.getMsName(), e.getMessage(), e);

            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName();
            }

            // 상세 에러는 서버 로그에만 기록
            String detailedError = errorMessage;
            if (e.getCause() != null) {
                detailedError += " (원인: " + e.getCause().getMessage() + ")";
            }

            history.setMhStatus("ERROR");
            history.setMhErrorMessage(detailedError.length() > 500 ? detailedError.substring(0, 500) : detailedError);

            // 클라이언트에는 일반적인 에러 메시지만 반환 (정보 노출 방지)
            result.put("status", "ERROR");
            result.put("error", "서비스 연결 실패");
            result.put("healthy", false);
        }

        // 이력 저장
        monitoringMapper.insertHistory(history);

        return result;
    }

    /**
     * 모든 활성화된 서비스 체크
     */
    public Map<String, Map<String, Object>> checkAllEnabledServices() {
        Map<String, Map<String, Object>> results = new HashMap<>();
        List<MonitoringService> services = getEnabledServices();

        for (MonitoringService service : services) {
            Map<String, Object> result = checkService(service);
            results.put(service.getMsName(), result);
        }

        return results;
    }

    /**
     * 서비스 이력 조회
     */
    public List<MonitoringHistory> getServiceHistory(Integer msId, int limit) {
        return monitoringMapper.findHistoryByServiceId(msId, limit);
    }

    /**
     * 최근 이력 조회
     */
    public List<MonitoringHistory> getRecentHistory(int hours) {
        return monitoringMapper.findRecentHistory(hours);
    }

    /**
     * 오래된 이력 삭제
     */
    public int cleanOldHistory(int days) {
        return monitoringMapper.deleteOldHistory(days);
    }

    /**
     * 날짜 범위로 서비스 이력 조회
     */
    public List<MonitoringHistory> getServiceHistoryByDateRange(Integer msId, String startDate, String endDate, int limit) {
        return monitoringMapper.findHistoryByDateRange(msId, startDate, endDate, limit);
    }

    /**
     * SSRF 방어: 허용된 네트워크(192.168.x.x, 루프백)만 접근 가능.
     * 클라우드 메타데이터 및 비허가 사설 대역(10.x, 172.16~31.x) 차단.
     */
    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("모니터링 URL이 비어 있습니다.");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("허용되지 않는 프로토콜: " + scheme);
            }
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("호스트가 비어 있습니다.");
            }
            InetAddress addr = InetAddress.getByName(host);
            String ip = addr.getHostAddress();

            // 허용: 루프백(127.x), 192.168.x.x, 공인 IP
            // 차단: 10.x.x.x, 172.16~31.x.x, 링크로컬(169.254.x), 메타데이터
            if (addr.isLinkLocalAddress()
                    || ip.startsWith("169.254.") || ip.startsWith("100.100.100.")) {
                throw new IllegalArgumentException("메타데이터 서비스 접근이 차단되었습니다: " + host);
            }
            if (ip.startsWith("10.")) {
                throw new IllegalArgumentException("허용되지 않는 사설 대역입니다: " + host);
            }
            if (addr.isSiteLocalAddress() && !ip.startsWith("192.168.")) {
                throw new IllegalArgumentException("허용되지 않는 사설 대역입니다: " + host);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("URL 검증 실패: " + e.getMessage(), e);
        }
    }
}
