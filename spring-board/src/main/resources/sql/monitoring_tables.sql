-- 외부 서비스 모니터링 설정 테이블
CREATE TABLE IF NOT EXISTS g5_monitoring_service (
    ms_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ms_name VARCHAR(100) NOT NULL COMMENT '서비스명',
    ms_url VARCHAR(500) NOT NULL COMMENT '모니터링 URL',
    ms_method VARCHAR(10) DEFAULT 'GET' COMMENT 'HTTP 메소드',
    ms_auth_type VARCHAR(20) DEFAULT 'NONE' COMMENT '인증타입 (NONE/BASIC/BEARER/HEADER)',
    ms_auth_username VARCHAR(255) COMMENT '인증 사용자명',
    ms_auth_password VARCHAR(255) COMMENT '인증 비밀번호',
    ms_auth_token TEXT COMMENT '인증 토큰/API키',
    ms_auth_header_name VARCHAR(100) COMMENT '커스텀 헤더명',
    ms_auth_header_value TEXT COMMENT '커스텀 헤더값',
    ms_timeout INT DEFAULT 5000 COMMENT '타임아웃(ms)',
    ms_expected_status INT DEFAULT 200 COMMENT '정상 상태코드',
    ms_expected_response TEXT COMMENT '예상 응답 내용 (JSON)',
    ms_check_interval INT DEFAULT 60 COMMENT '체크 주기(초)',
    ms_enabled TINYINT(1) DEFAULT 1 COMMENT '활성화 여부',
    ms_alert_enabled TINYINT(1) DEFAULT 0 COMMENT '알림 활성화',
    ms_alert_email VARCHAR(255) COMMENT '알림 이메일',
    ms_description TEXT COMMENT '설명',
    ms_created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    ms_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_enabled (ms_enabled),
    INDEX idx_name (ms_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='외부 서비스 모니터링 설정';

-- 모니터링 이력 테이블
CREATE TABLE IF NOT EXISTS g5_monitoring_history (
    mh_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ms_id INT NOT NULL COMMENT '서비스 ID',
    mh_status VARCHAR(10) NOT NULL COMMENT '상태 (UP/DOWN/ERROR)',
    mh_status_code INT COMMENT 'HTTP 상태코드',
    mh_response_time INT COMMENT '응답시간(ms)',
    mh_cpu_usage DECIMAL(5,2) COMMENT 'CPU 사용량(%)',
    mh_error_message TEXT COMMENT '에러 메시지',
    mh_checked_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '체크 일시',
    INDEX idx_service (ms_id),
    INDEX idx_status (mh_status),
    INDEX idx_checked_at (mh_checked_at),
    FOREIGN KEY (ms_id) REFERENCES g5_monitoring_service(ms_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='모니터링 이력';
