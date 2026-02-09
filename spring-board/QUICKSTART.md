# 빠른 시작 가이드

## 3단계로 시작하기

### 1️⃣ 환경 초기화

```bash
./docker-init.sh
```

이 명령어는 자동으로:
- 필요한 디렉토리 생성 (`mysql_data`, `data`, `nginx/ssl`)
- `.env` 파일 생성
- 개발용 SSL 인증서 생성
- Docker 설치 확인

### 2️⃣ 비밀번호 설정

```bash
vi .env  # 또는 nano .env
```

**필수 변경 항목:**
```bash
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_PASSWORD=your_secure_user_password
```

### 3️⃣ 실행

```bash
docker-compose up -d
```

## 접속 주소

✅ **애플리케이션**: http://localhost/datahub/board

## 로그 확인

```bash
# 전체 로그
docker-compose logs -f

# Spring Boot만
docker-compose logs -f spring-board

# 최근 100줄만
docker-compose logs --tail=100 spring-board
```

## 주요 명령어

### 상태 확인
```bash
docker-compose ps
```

### 중지
```bash
docker-compose down
```

### 재시작
```bash
docker-compose restart
```

### 재빌드
```bash
docker-compose up -d --build
```

### 전체 삭제 (주의: 데이터 손실)
```bash
docker-compose down -v
```

## 서비스 구성

```
┌─────────────────────────────────────────┐
│         http://localhost (80)           │
│         https://localhost (443)         │
└─────────────────┬───────────────────────┘
                  │
          ┌───────▼────────┐
          │     Nginx      │ (리버스 프록시)
          │   (컨테이너)    │
          └───────┬────────┘
                  │
          ┌───────▼────────┐
          │  Spring Boot   │ (포트 8080)
          │   (컨테이너)    │
          └───────┬────────┘
                  │
          ┌───────▼────────┐
          │   MySQL 8.0    │ (포트 3306)
          │   (컨테이너)    │
          └────────────────┘
```

## 디렉토리 구조

```
spring-board/
├── docker-compose.yml      # Docker Compose 설정
├── Dockerfile              # Spring Boot 이미지 빌드
├── .env                    # 환경 변수 (생성 필요)
├── .env.example            # 환경 변수 예제
├── mysql_data/             # MySQL 데이터 (자동 생성)
├── data/                   # 애플리케이션 데이터 (자동 생성)
├── nginx/
│   ├── default.conf        # Nginx 설정
│   ├── ssl/                # SSL 인증서 (자동 생성)
│   └── README.md           # Nginx 상세 가이드
├── docker-init.sh          # 초기화 스크립트
├── docker-build.sh         # 빌드 스크립트
└── docker-run.sh           # 실행 관리 스크립트
```

## 문제 해결

### 포트 충돌

이미 80, 443, 3306, 8080 포트를 사용 중이라면:

1. **기존 서비스 중지** 또는
2. **포트 변경**: `docker-compose.yml`에서 포트 매핑 수정

```yaml
ports:
  - "8080:80"    # 80 대신 8080 사용
  - "8443:443"   # 443 대신 8443 사용
```

### 502 Bad Gateway

Spring Boot가 시작되는 데 시간이 걸릴 수 있습니다:

```bash
# Spring Boot 로그 확인
docker-compose logs -f spring-board

# "Started SpringBoardApplication" 메시지를 기다림
```

### 데이터베이스 연결 오류

```bash
# MySQL 로그 확인
docker-compose logs -f db

# MySQL 준비 상태 확인
docker-compose exec db mysqladmin ping -h localhost -u root -p
```

### 메모리 부족

`.env` 파일에서 메모리 조정:

```bash
JAVA_OPTS=-Xms256m -Xmx1024m
```

## 데이터 백업

```bash
# MySQL 백업
docker-compose exec db mysqldump -u root -p gnuboard > backup.sql

# 또는 전체 볼륨 백업
docker run --rm \
  -v spring-board_mysql_data:/data \
  -v $(pwd)/backup:/backup \
  alpine tar czf /backup/mysql-$(date +%Y%m%d).tar.gz /data
```

## 프로덕션 배포 시 체크리스트

- [ ] `.env` 파일의 모든 비밀번호 변경
- [ ] Let's Encrypt 인증서로 SSL 설정 (nginx/README.md 참조)
- [ ] `SPRING_PROFILES_ACTIVE=prod` 확인
- [ ] 방화벽 설정 (필요한 포트만 개방)
- [ ] 로그 모니터링 설정
- [ ] 자동 백업 스크립트 설정
- [ ] 메모리/CPU 리소스 제한 설정

## 더 많은 정보

- **상세 가이드**: [README.Docker.md](README.Docker.md)
- **Nginx 설정**: [nginx/README.md](nginx/README.md)
- **OCR 설정**: [OCR_SETUP_GUIDE.md](OCR_SETUP_GUIDE.md)

## 도움말

```bash
# 편리한 관리 스크립트
./docker-run.sh          # 사용법 보기
./docker-run.sh start    # 시작
./docker-run.sh logs     # 로그
./docker-run.sh stop     # 중지
./docker-run.sh shell    # 컨테이너 접속
```
