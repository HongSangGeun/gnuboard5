package com.deepcode.springboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 모니터링 이력
 */
@Data
public class MonitoringHistory {
    private Long mhId;
    private Integer msId;
    private String mhStatus;
    private Integer mhStatusCode;
    private Integer mhResponseTime;
    private Double mhCpuUsage;
    private String mhErrorMessage;
    private LocalDateTime mhCheckedAt;
}
