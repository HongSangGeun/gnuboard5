# Docker 배포 가이드

이 가이드는 Intel/AMD64 CPU 기반 Ubuntu 환경에서 애플리케이션을 Docker로 실행하는 방법을 설명합니다.

## 사전 요구사항

- Docker 20.10 이상
- Docker Compose 2.0 이상
- 최소 4GB RAM (권장: 8GB 이상)

## 빠른 시작

### 1. 초기 설정

```bash
# 환경 초기화 (디렉토리 생성, .env 파일 생성, SSL 인증서 생성)
./docker-init.sh
```

### 2. 환경 변수 설정

`.env` 파일을 열어 필요한 값을 설정하세요:

```bash
# 데이터베이스 비밀번호는 반드시 변경하세요
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_PASSWORD=your_board_password

# 메일 설정 (선택사항)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

### 3. Docker Compose로 실행

```bash
# 빌드 및 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f spring-board

# 상태 확인
docker-compose ps
```

### 4. 애플리케이션 접속

- **HTTP**: http://localhost/datahub/board
- **HTTPS**: https://localhost/datahub/board (자체 서명 인증서)
- **Health Check**: http://localhost/health

## Docker 명령어

### 개별 빌드

```bash
# Intel/AMD64 플랫폼으로 빌드
docker build --platform linux/amd64 -t gnuboard-app:latest .

# 실행
docker run -d \
  --name gnuboard-app \
  --platform linux/amd64 \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/myboard" \
  -e SPRING_DATASOURCE_USERNAME=boarduser \
  -e SPRING_DATASOURCE_PASSWORD=boardpassword \
  gnuboard-app:latest
```

### Mac (Apple Silicon)에서 Intel 이미지 빌드

```bash
# buildx 사용 (Mac M1/M2/M3에서)
docker buildx build --platform linux/amd64 -t gnuboard-app:latest .

# 또는 --platform 옵션 사용
docker build --platform linux/amd64 -t gnuboard-app:latest .
```

### 디버그 모드로 실행

```bash
# docker-compose.yml 수정 또는 환경 변수 오버라이드
docker-compose run -e SPRING_PROFILES_ACTIVE=debug app
```

## 볼륨 관리

### 데이터 백업

```bash
# MySQL 데이터 백업
docker run --rm \
  --volumes-from gnuboard-mysql \
  -v $(pwd)/backup:/backup \
  ubuntu tar czf /backup/mysql-backup-$(date +%Y%m%d).tar.gz /var/lib/mysql

# 애플리케이션 데이터 백업
docker run --rm \
  --volumes-from gnuboard-app \
  -v $(pwd)/backup:/backup \
  ubuntu tar czf /backup/app-data-backup-$(date +%Y%m%d).tar.gz /app/data
```

### 데이터 복원

```bash
# MySQL 데이터 복원
docker run --rm \
  --volumes-from gnuboard-mysql \
  -v $(pwd)/backup:/backup \
  ubuntu bash -c "cd /var/lib && tar xzf /backup/mysql-backup-YYYYMMDD.tar.gz"
```

## 문제 해결

### 컨테이너 로그 확인

```bash
# 애플리케이션 로그
docker-compose logs -f app

# MySQL 로그
docker-compose logs -f mysql

# 최근 100줄만 보기
docker-compose logs --tail=100 app
```

### 컨테이너 내부 접속

```bash
# 애플리케이션 컨테이너
docker-compose exec app bash

# MySQL 컨테이너
docker-compose exec mysql bash

# MySQL 클라이언트 접속
docker-compose exec mysql mysql -u root -p
```

### 전체 재시작

```bash
# 모든 컨테이너 중지 및 삭제
docker-compose down

# 볼륨까지 삭제 (주의: 데이터 손실)
docker-compose down -v

# 이미지 재빌드 후 시작
docker-compose up -d --build
```

### 메모리 부족 오류

`JAVA_OPTS` 환경 변수를 조정하세요:

```bash
# .env 파일에서
JAVA_OPTS=-Xms256m -Xmx1024m
```

## 프로덕션 배포 권장사항

1. **환경 변수 보안**
   - `.env` 파일을 Git에 커밋하지 마세요
   - 강력한 비밀번호 사용
   - 프로덕션에서는 Docker Secrets 또는 환경 변수 주입 사용

2. **리소스 제한**
   ```yaml
   # docker-compose.yml에 추가
   services:
     app:
       deploy:
         resources:
           limits:
             cpus: '2'
             memory: 2G
   ```

3. **로그 관리**
   ```yaml
   # docker-compose.yml에 추가
   services:
     app:
       logging:
         driver: "json-file"
         options:
           max-size: "10m"
           max-file: "3"
   ```

4. **자동 재시작**
   - `restart: unless-stopped` 정책 사용 (이미 설정됨)

5. **헬스 체크**
   - 이미 설정되어 있음 (30초 간격)

## 성능 최적화

### 빌드 캐시 활용

```bash
# 캐시를 사용하여 빌드
docker-compose build

# 캐시 없이 완전 재빌드
docker-compose build --no-cache
```

### 멀티스테이지 빌드

현재 Dockerfile은 이미 멀티스테이지 빌드를 사용하여:
- 빌드 단계: Gradle로 JAR 생성
- 런타임 단계: 최소한의 런타임만 포함

이를 통해 최종 이미지 크기가 크게 감소합니다.

## 모니터링

### 리소스 사용량 확인

```bash
# 실시간 통계
docker stats gnuboard-app gnuboard-mysql

# 디스크 사용량
docker system df
```

### 헬스 체크 상태

```bash
docker inspect --format='{{json .State.Health}}' gnuboard-app | jq
```

## 업데이트

```bash
# 코드 변경 후 재배포
git pull
docker-compose down
docker-compose up -d --build

# 또는 롤링 업데이트 (다운타임 최소화)
docker-compose up -d --no-deps --build app
```
