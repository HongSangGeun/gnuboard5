# Nginx 설정 가이드

## 기본 설정

현재 nginx는 리버스 프록시로 동작하며 다음 기능을 제공합니다:

- Spring Boot 애플리케이션 프록시 (포트 8080 → 80)
- 파일 업로드 최대 300MB 지원
- Gzip 압축
- 정적 파일 캐싱 (1년)
- 헬스 체크 엔드포인트 (/health)

## 접속 주소

- HTTP: http://localhost/datahub/board
- 헬스 체크: http://localhost/health

## SSL/HTTPS 설정

### 1. SSL 인증서 준비

자체 서명 인증서 생성 (개발/테스트용):

```bash
mkdir -p nginx/ssl
cd nginx/ssl

# 자체 서명 인증서 생성
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout key.pem \
  -out cert.pem \
  -subj "/C=KR/ST=Seoul/L=Seoul/O=MyOrganization/CN=localhost"
```

Let's Encrypt 인증서 (프로덕션용):

```bash
# certbot 설치 및 인증서 발급
sudo apt-get install certbot
sudo certbot certonly --standalone -d your-domain.com

# 인증서 복사
mkdir -p nginx/ssl
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem nginx/ssl/key.pem
```

### 2. HTTPS 설정 활성화

`nginx/default.conf` 파일에서 HTTPS 섹션의 주석을 해제하고 도메인 변경:

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;  # 여기를 실제 도메인으로 변경

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    # ... 나머지 설정 ...
}
```

HTTP → HTTPS 리다이렉트 활성화:

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 여기를 실제 도메인으로 변경
    return 301 https://$server_name$request_uri;
}
```

### 3. nginx 재시작

```bash
docker-compose restart nginx
```

## 커스텀 설정

### 업로드 크기 변경

`nginx/default.conf`:

```nginx
client_max_body_size 500M;  # 500MB로 변경
```

### 타임아웃 변경

```nginx
proxy_connect_timeout 600;
proxy_send_timeout 600;
proxy_read_timeout 600;
```

### 로그 레벨 변경

```nginx
error_log /var/log/nginx/error.log warn;  # debug, info, notice, warn, error, crit
```

## 로그 확인

```bash
# nginx 컨테이너 로그
docker-compose logs -f nginx

# nginx 내부 로그
docker-compose exec nginx tail -f /var/log/nginx/access.log
docker-compose exec nginx tail -f /var/log/nginx/error.log
```

## 문제 해결

### 502 Bad Gateway

Spring Boot 애플리케이션이 준비되지 않은 경우 발생:

```bash
# Spring Boot 로그 확인
docker-compose logs -f spring-board

# 헬스 체크 확인
docker-compose exec spring-board curl http://localhost:8080/datahub/board/actuator/health
```

### 413 Request Entity Too Large

업로드 파일이 너무 큰 경우:

1. `nginx/default.conf`에서 `client_max_body_size` 확인
2. Spring Boot의 `application.yml`에서 `spring.servlet.multipart.max-file-size` 확인
3. 두 설정 모두 충분히 큰 값으로 설정

### 설정 파일 문법 확인

```bash
docker-compose exec nginx nginx -t
```

### nginx 리로드 (재시작 없이 설정 적용)

```bash
docker-compose exec nginx nginx -s reload
```

## 성능 튜닝

### Worker 프로세스 수 조정

nginx 이미지를 커스터마이징하려면 `nginx/nginx.conf` 생성:

```nginx
user nginx;
worker_processes auto;  # CPU 코어 수만큼 자동 설정

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    include /etc/nginx/conf.d/*.conf;
}
```

`docker-compose.yml`에서 볼륨 추가:

```yaml
volumes:
  - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
  - ./nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
```

### 캐시 설정

정적 파일 캐시 디렉토리 추가:

```nginx
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=my_cache:10m max_size=1g inactive=60m;

location /datahub/board/static/ {
    proxy_cache my_cache;
    proxy_cache_valid 200 1d;
    proxy_pass http://spring-board;
}
```

## 보안 설정

### 기본 보안 헤더 추가

```nginx
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "no-referrer-when-downgrade" always;
```

### Rate Limiting

```nginx
limit_req_zone $binary_remote_addr zone=one:10m rate=10r/s;

location /datahub/board/api/ {
    limit_req zone=one burst=20;
    proxy_pass http://spring-board;
}
```
