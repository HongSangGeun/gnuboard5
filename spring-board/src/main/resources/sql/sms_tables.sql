-- SMS 설정 테이블
CREATE TABLE IF NOT EXISTS g5_sms5_config (
    cf_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cf_phone VARCHAR(20) DEFAULT '' COMMENT '발신번호',
    cf_icode VARCHAR(50) DEFAULT '' COMMENT 'SMS 서비스 ID',
    cf_admin VARCHAR(50) DEFAULT '' COMMENT '관리자',
    cf_hp VARCHAR(20) DEFAULT '' COMMENT '관리자 휴대폰',
    cf_email VARCHAR(100) DEFAULT '' COMMENT '관리자 이메일',
    cf_skin VARCHAR(50) DEFAULT 'basic' COMMENT '스킨',
    cf_datetime DATETIME DEFAULT NULL COMMENT '최종 수정일',
    cf_ip VARCHAR(50) DEFAULT '' COMMENT '접속 IP',
    cf_click_count INT DEFAULT 0 COMMENT '클릭 카운트',
    cf_write VARCHAR(255) DEFAULT '' COMMENT '작성 권한',
    cf_reply VARCHAR(255) DEFAULT '' COMMENT '답변 권한',
    cf_use CHAR(1) DEFAULT '1' COMMENT '사용 여부',
    cf_sms_type VARCHAR(10) DEFAULT 'SMS' COMMENT 'SMS 유형 (SMS, LMS, MMS)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SMS 설정';

-- SMS 발송 이력 테이블
CREATE TABLE IF NOT EXISTS g5_sms5_write (
    wr_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    wr_message TEXT COMMENT '메시지 내용',
    wr_total INT DEFAULT 0 COMMENT '총 발송 건수',
    wr_success INT DEFAULT 0 COMMENT '성공 건수',
    wr_failure INT DEFAULT 0 COMMENT '실패 건수',
    wr_datetime DATETIME DEFAULT NULL COMMENT '발송 일시',
    INDEX idx_datetime (wr_datetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SMS 발송 이력';

-- 기본 설정 데이터 삽입 (설정이 없을 경우에만)
INSERT INTO g5_sms5_config (cf_id, cf_use, cf_sms_type)
SELECT 1, '0', 'SMS'
WHERE NOT EXISTS (SELECT 1 FROM g5_sms5_config WHERE cf_id = 1);
