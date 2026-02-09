# CPU 사용량 모니터링 기능 추가

## 개요
외부 모니터링 체크 시점의 CPU 사용량을 기록하고 그래프에 표시하는 기능이 추가되었습니다.

## 데이터베이스 마이그레이션

다음 SQL을 실행하여 데이터베이스 스키마를 업데이트하세요:

```sql
-- g5_monitoring_history 테이블에 CPU 컬럼 추가
ALTER TABLE g5_monitoring_history
ADD COLUMN mh_cpu_usage DECIMAL(5,2) NULL COMMENT 'CPU 사용량 (%)'
AFTER mh_response_time;
```

또는 제공된 SQL 파일을 직접 실행:
```bash
mysql -u [username] -p [database_name] < sql/add_monitoring_cpu_usage.sql
```

## 적용 후 확인사항

1. 애플리케이션 재시작
2. 대시보드의 시스템 모니터링 카드 확인
3. 차트에 다음 두 가지 지표가 표시됨:
   - **응답시간 (ms)** - 파란색 선, 왼쪽 Y축
   - **CPU 사용량 (%)** - 주황색 선, 오른쪽 Y축

## 구현 내용

### 백엔드
- `MonitoringHistory`: CPU 필드 추가
- `MonitoringServiceManager`: 모니터링 시점에 시스템 CPU 캡처
- `MonitoringApiController`: API 응답에 CPU 데이터 포함

### 프론트엔드
- Chart.js 이중 Y축 차트로 업그레이드
- 응답시간과 CPU를 동시에 표시
- 범례로 두 지표 구분

## 기술 상세

CPU 사용량은 Java의 `OperatingSystemMXBean`을 통해 측정됩니다:
- 실시간 시스템 CPU 로드를 백분율로 측정
- 모니터링 체크가 실행되는 시점의 CPU 상태 기록
- 소수점 1자리까지 저장 (예: 45.3%)
