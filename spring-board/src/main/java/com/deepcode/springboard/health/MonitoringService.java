package com.deepcode.springboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 외부 서비스 모니터링 설정
 */
@Data
public class MonitoringService {
    private Integer msId;
    private String msName;
    private String msUrl;
    private String msMethod;
    private String msAuthType;
    private String msAuthUsername;
    private String msAuthPassword;
    private String msAuthToken;
    private String msAuthHeaderName;
    private String msAuthHeaderValue;
    private Integer msTimeout;
    private Integer msExpectedStatus;
    private String msExpectedResponse;
    private Integer msCheckInterval;
    private Boolean msEnabled;
    private Boolean msAlertEnabled;
    private String msAlertEmail;
    private String msDescription;
    private LocalDateTime msCreatedAt;
    private LocalDateTime msUpdatedAt;
}
