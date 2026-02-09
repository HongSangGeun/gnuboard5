-- 외부 모니터링 이력 테이블에 CPU 사용량 컬럼 추가
ALTER TABLE g5_monitoring_history
ADD COLUMN mh_cpu_usage DECIMAL(5,2) NULL COMMENT 'CPU 사용량 (%)'
AFTER mh_response_time;
