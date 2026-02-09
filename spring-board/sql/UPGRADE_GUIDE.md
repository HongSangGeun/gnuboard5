# CPU 모니터링 기능 업그레이드 가이드

## 적용 완료 내용

✅ **설치 프로그램**: `monitoring_tables.sql`에 CPU 컬럼 포함
✅ **DB 업그레이드**: 자동 컬럼 추가 지원
✅ **백엔드**: CPU 캡처 및 저장 로직
✅ **프론트엔드**: 이중 Y축 차트 (응답시간 + CPU)

## 기존 시스템 업그레이드 방법

### 방법 1: 관리자 메뉴에서 자동 업그레이드 (권장)

1. 관리자 로그인
2. **[관리자] → [환경설정] → [DB 업그레이드]** 메뉴 접속
3. **[업그레이드 실행]** 버튼 클릭
4. `g5_monitoring_history` 테이블에 `mh_cpu_usage` 컬럼 자동 추가됨

### 방법 2: 수동 SQL 실행

```bash
# MySQL 접속
mysql -u [사용자명] -p [데이터베이스명]

# SQL 실행
ALTER TABLE g5_monitoring_history
ADD COLUMN mh_cpu_usage DECIMAL(5,2) NULL COMMENT 'CPU 사용량 (%)'
AFTER mh_response_time;
```

또는 파일로 실행:
```bash
mysql -u [사용자명] -p [데이터베이스명] < sql/add_monitoring_cpu_usage.sql
```

## 신규 설치

신규 설치 시에는 별도 작업 없이 자동으로 CPU 컬럼이 포함됩니다.

설치 프로그램이 `monitoring_tables.sql`을 실행하면서 CPU 컬럼이 생성됩니다.

## 확인 방법

### 1. 데이터베이스 확인
```sql
-- 컬럼 추가 확인
DESCRIBE g5_monitoring_history;

-- mh_cpu_usage DECIMAL(5,2) 컬럼이 있어야 함
```

### 2. 애플리케이션 재시작
```bash
# 애플리케이션 종료 후 재시작
./gradlew bootRun
```

### 3. 대시보드 확인
- 홈 화면의 **시스템 모니터링 카드** 확인
- 차트에 두 개의 선이 표시되어야 함:
  - **파란색**: 응답시간 (ms) - 왼쪽 Y축
  - **주황색**: CPU 사용량 (%) - 오른쪽 Y축

## 동작 원리

1. **모니터링 체크 시점**에 시스템 CPU 사용량 캡처
2. 응답시간과 함께 데이터베이스에 저장
3. API에서 최근 24시간 데이터 조회 시 CPU 포함
4. Chart.js 이중 Y축으로 두 지표 동시 표시

## 문제 해결

### CPU 데이터가 표시되지 않는 경우

1. **컬럼 추가 확인**
   ```sql
   SELECT COLUMN_NAME, DATA_TYPE
   FROM information_schema.COLUMNS
   WHERE TABLE_NAME = 'g5_monitoring_history'
   AND COLUMN_NAME = 'mh_cpu_usage';
   ```

2. **애플리케이션 재시작 확인**
   - 코드 변경사항이 반영되려면 반드시 재시작 필요

3. **새 모니터링 데이터 수집 대기**
   - 기존 데이터에는 CPU 값이 없음 (NULL)
   - 업그레이드 후 새로 수집되는 데이터부터 CPU 표시

4. **브라우저 캐시 삭제**
   - 차트 렌더링 코드가 갱신되지 않았다면 새로고침 (Ctrl+F5)

## 참고사항

- CPU 사용량은 시스템 전체 CPU 로드를 측정합니다
- 0~100% 범위의 값으로 저장됩니다
- 소수점 1자리까지 정밀도를 유지합니다
- CPU 측정 실패 시 NULL로 저장되며 차트에서는 0으로 표시됩니다
