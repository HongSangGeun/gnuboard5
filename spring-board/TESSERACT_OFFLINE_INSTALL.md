# Tesseract 오프라인 설치 가이드 (Linux Intel)

인터넷 접속이 불가능한 운영 서버에 Tesseract OCR을 설치하는 방법입니다.

## 목차
1. [Ubuntu/Debian 오프라인 설치](#ubuntudebian-오프라인-설치)
2. [CentOS/RHEL 오프라인 설치](#centosrhel-오프라인-설치)
3. [애플리케이션 번들 방식](#애플리케이션-번들-방식-권장)
4. [설치 확인 및 설정](#설치-확인-및-설정)

---

## Ubuntu/Debian 오프라인 설치

### 1단계: 패키지 다운로드 (인터넷 연결된 동일 버전 서버)

```bash
# 작업 디렉토리 생성
mkdir -p ~/tesseract-offline
cd ~/tesseract-offline

# 운영 서버와 동일한 Ubuntu 버전 확인
lsb_release -a

# apt-get을 사용하여 패키지만 다운로드 (설치하지 않음)
# --download-only: 다운로드만 수행
# -y: 자동 yes
# --print-uris: 다운로드 URL 출력 (선택사항)

# Tesseract 및 의존성 다운로드
sudo apt-get update
sudo apt-get install --download-only -y \
  tesseract-ocr \
  tesseract-ocr-kor \
  tesseract-ocr-eng \
  libtesseract4 \
  libleptonica5 \
  libtesseract-dev

# 다운로드된 패키지는 /var/cache/apt/archives/ 에 저장됨
# 패키지 복사
sudo cp /var/cache/apt/archives/*.deb ~/tesseract-offline/

# 소유권 변경
sudo chown -R $USER:$USER ~/tesseract-offline/

# 압축
tar -czf tesseract-offline-ubuntu.tar.gz ~/tesseract-offline/

# 파일 리스트 확인
ls -lh tesseract-offline-ubuntu.tar.gz
```

### 2단계: 운영 서버로 전송

```bash
# USB, SCP, 또는 다른 방법으로 전송
# 예: USB 마운트 후
cp tesseract-offline-ubuntu.tar.gz /mnt/usb/
```

### 3단계: 운영 서버에서 설치

```bash
# 압축 해제
tar -xzf tesseract-offline-ubuntu.tar.gz
cd tesseract-offline

# 패키지 설치 (의존성 순서 중요)
# 먼저 의존성 패키지부터
sudo dpkg -i libleptonica*.deb
sudo dpkg -i libtesseract*.deb

# Tesseract 본체 설치
sudo dpkg -i tesseract-ocr-*.deb
sudo dpkg -i tesseract-ocr_*.deb

# 의존성 문제 해결 (필요시)
sudo apt-get install -f

# 설치 확인
tesseract --version
```

---

## CentOS/RHEL 오프라인 설치

### 1단계: 패키지 다운로드 (인터넷 연결된 동일 버전 서버)

```bash
# 작업 디렉토리 생성
mkdir -p ~/tesseract-offline
cd ~/tesseract-offline

# OS 버전 확인
cat /etc/redhat-release

# EPEL 저장소 추가 (필요시)
sudo yum install epel-release -y

# yumdownloader로 패키지와 의존성 다운로드
# yum-utils 먼저 설치
sudo yum install yum-utils -y

# Tesseract 및 모든 의존성 다운로드
yumdownloader --resolve --destdir=. \
  tesseract \
  tesseract-langpack-kor \
  tesseract-langpack-eng \
  leptonica

# 압축
tar -czf tesseract-offline-centos.tar.gz *.rpm

# 파일 리스트 확인
ls -lh tesseract-offline-centos.tar.gz
```

### 2단계: 운영 서버로 전송

```bash
# USB, SCP, 또는 다른 방법으로 전송
cp tesseract-offline-centos.tar.gz /mnt/usb/
```

### 3단계: 운영 서버에서 설치

```bash
# 압축 해제
tar -xzf tesseract-offline-centos.tar.gz
cd tesseract-offline

# 모든 RPM 패키지 설치
sudo rpm -Uvh *.rpm

# 또는 yum을 사용하여 로컬 설치 (의존성 자동 해결)
sudo yum localinstall -y *.rpm

# 설치 확인
tesseract --version
```

---

## 애플리케이션 번들 방식 (권장)

인터넷 연결된 서버에서 Tesseract를 미리 설치하고, 필요한 파일만 추출하여 애플리케이션과 함께 배포하는 방식입니다.

### 1단계: Tesseract 바이너리 및 라이브러리 추출

```bash
# 인터넷 연결된 서버에서 Tesseract 설치
# Ubuntu
sudo apt-get install -y tesseract-ocr tesseract-ocr-kor tesseract-ocr-eng

# CentOS
sudo yum install -y tesseract tesseract-langpack-kor tesseract-langpack-eng

# 필요한 파일 복사
mkdir -p ~/tesseract-bundle/{bin,lib,share}

# 실행 파일
cp /usr/bin/tesseract ~/tesseract-bundle/bin/

# 라이브러리 찾기 및 복사
ldd /usr/bin/tesseract

# Ubuntu
cp /usr/lib/x86_64-linux-gnu/libtesseract.so* ~/tesseract-bundle/lib/
cp /usr/lib/x86_64-linux-gnu/liblept.so* ~/tesseract-bundle/lib/
cp -r /usr/share/tesseract-ocr ~/tesseract-bundle/share/

# CentOS
cp /usr/lib64/libtesseract.so* ~/tesseract-bundle/lib/
cp /usr/lib64/liblept.so* ~/tesseract-bundle/lib/
cp -r /usr/share/tessdata ~/tesseract-bundle/share/

# 압축
cd ~
tar -czf tesseract-bundle.tar.gz tesseract-bundle/

ls -lh tesseract-bundle.tar.gz
```

### 2단계: 운영 서버에 배포

```bash
# 압축 해제
tar -xzf tesseract-bundle.tar.gz

# 애플리케이션 디렉토리에 배치
sudo mkdir -p /opt/app
sudo mv tesseract-bundle /opt/app/tesseract

# 권한 설정
sudo chmod +x /opt/app/tesseract/bin/tesseract
```

### 3단계: application.yml 설정

```yaml
app:
  ocr:
    enabled: true
    tesseract-lib-path: /opt/app/tesseract/lib
    tessdata-path: /opt/app/tesseract/share/tessdata
    language: kor+eng
    engine-mode: 1
    page-seg-mode: 3
```

### 4단계: 환경 변수 설정

```bash
# /etc/profile.d/tesseract.sh 생성
sudo tee /etc/profile.d/tesseract.sh > /dev/null <<'EOF'
export PATH=/opt/app/tesseract/bin:$PATH
export LD_LIBRARY_PATH=/opt/app/tesseract/lib:$LD_LIBRARY_PATH
export TESSDATA_PREFIX=/opt/app/tesseract/share/tessdata
EOF

# 적용
source /etc/profile.d/tesseract.sh

# 또는 애플리케이션 시작 스크립트에 추가
# start.sh
#!/bin/bash
export LD_LIBRARY_PATH=/opt/app/tesseract/lib:$LD_LIBRARY_PATH
export TESSDATA_PREFIX=/opt/app/tesseract/share/tessdata
java -jar spring-board.jar
```

---

## Docker 이미지 방식 (추천)

인터넷 연결된 환경에서 Docker 이미지를 빌드하고, tar 파일로 저장하여 오프라인 서버에 로드하는 방식입니다.

### 1단계: Dockerfile 작성

```dockerfile
FROM eclipse-temurin:17-jre

# Tesseract 설치
RUN apt-get update && \
    apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor \
    tesseract-ocr-eng \
    libtesseract4 && \
    rm -rf /var/lib/apt/lists/*

# 애플리케이션 복사
COPY target/spring-board.jar /app/app.jar

WORKDIR /app

# 환경 변수 설정
ENV TESSERACT_LIB_PATH=/usr/lib/x86_64-linux-gnu
ENV TESSDATA_PATH=/usr/share/tesseract-ocr/4.00/tessdata
ENV OCR_LANGUAGE=kor+eng

# 포트 노출
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

### 2단계: 이미지 빌드 및 저장 (인터넷 연결 서버)

```bash
# 이미지 빌드
docker build -t spring-board:latest .

# 이미지를 tar 파일로 저장
docker save spring-board:latest > spring-board-image.tar

# 압축 (선택사항)
gzip spring-board-image.tar

# 파일 크기 확인
ls -lh spring-board-image.tar.gz
```

### 3단계: 운영 서버에 로드

```bash
# 압축 해제 (압축했을 경우)
gunzip spring-board-image.tar.gz

# Docker 이미지 로드
docker load < spring-board-image.tar

# 확인
docker images | grep spring-board

# 실행
docker run -d \
  -p 8080:8080 \
  -v /opt/app/data:/app/data \
  --name spring-board \
  spring-board:latest
```

---

## 설치 확인 및 설정

### 1. Tesseract 설치 확인

```bash
# 버전 확인
tesseract --version

# 설치된 언어 확인
tesseract --list-langs

# 테스트 (샘플 이미지로)
tesseract test.png output -l kor+eng
cat output.txt
```

### 2. 라이브러리 경로 확인

```bash
# Ubuntu/Debian
ls -l /usr/lib/x86_64-linux-gnu/libtesseract.so*
ls -l /usr/share/tesseract-ocr/4.00/tessdata/

# CentOS/RHEL
ls -l /usr/lib64/libtesseract.so*
ls -l /usr/share/tessdata/

# 번들 방식
ls -l /opt/app/tesseract/lib/libtesseract.so*
ls -l /opt/app/tesseract/share/tessdata/
```

### 3. 언어 데이터 확인

```bash
# 필수 파일 확인
ls -lh /usr/share/tesseract-ocr/4.00/tessdata/kor.traineddata
ls -lh /usr/share/tesseract-ocr/4.00/tessdata/eng.traineddata

# 또는 번들 경로
ls -lh /opt/app/tesseract/share/tessdata/kor.traineddata
ls -lh /opt/app/tesseract/share/tessdata/eng.traineddata
```

### 4. application.yml 설정

#### Ubuntu/Debian (시스템 설치)

```yaml
app:
  ocr:
    enabled: true
    tesseract-lib-path: /usr/lib/x86_64-linux-gnu
    tessdata-path: /usr/share/tesseract-ocr/4.00/tessdata
    language: kor+eng
    engine-mode: 1
    page-seg-mode: 3
```

#### CentOS/RHEL (시스템 설치)

```yaml
app:
  ocr:
    enabled: true
    tesseract-lib-path: /usr/lib64
    tessdata-path: /usr/share/tessdata
    language: kor+eng
    engine-mode: 1
    page-seg-mode: 3
```

#### 번들 방식

```yaml
app:
  ocr:
    enabled: true
    tesseract-lib-path: /opt/app/tesseract/lib
    tessdata-path: /opt/app/tesseract/share/tessdata
    language: kor+eng
    engine-mode: 1
    page-seg-mode: 3
```

---

## 애플리케이션 시작 스크립트

### start.sh

```bash
#!/bin/bash

# Tesseract 라이브러리 경로 설정
export LD_LIBRARY_PATH=/opt/app/tesseract/lib:$LD_LIBRARY_PATH
export TESSDATA_PREFIX=/opt/app/tesseract/share/tessdata

# Java 홈 설정 (필요시)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk

# 애플리케이션 시작
$JAVA_HOME/bin/java \
  -Xms512m \
  -Xmx2g \
  -Dspring.profiles.active=prod \
  -jar spring-board.jar \
  >> /var/log/spring-board/app.log 2>&1 &

echo $! > /var/run/spring-board.pid
echo "Spring Board started with PID $(cat /var/run/spring-board.pid)"
```

### systemd 서비스 (권장)

```ini
# /etc/systemd/system/spring-board.service
[Unit]
Description=Spring Board Application
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/app
Environment="LD_LIBRARY_PATH=/opt/app/tesseract/lib"
Environment="TESSDATA_PREFIX=/opt/app/tesseract/share/tessdata"
Environment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk"
ExecStart=/usr/lib/jvm/java-17-openjdk/bin/java -jar spring-board.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 서비스 등록 및 시작
sudo systemctl daemon-reload
sudo systemctl enable spring-board
sudo systemctl start spring-board

# 상태 확인
sudo systemctl status spring-board

# 로그 확인
sudo journalctl -u spring-board -f
```

---

## 문제 해결

### 라이브러리를 찾을 수 없음

**오류:**
```
error while loading shared libraries: libtesseract.so.4
```

**해결:**
```bash
# 라이브러리 경로 추가
export LD_LIBRARY_PATH=/opt/app/tesseract/lib:$LD_LIBRARY_PATH

# 영구 설정
sudo tee -a /etc/ld.so.conf.d/tesseract.conf <<< "/opt/app/tesseract/lib"
sudo ldconfig

# 확인
ldconfig -p | grep tesseract
```

### 언어 데이터를 찾을 수 없음

**오류:**
```
Error opening data file /usr/share/tessdata/kor.traineddata
```

**해결:**
```bash
# 환경 변수 설정
export TESSDATA_PREFIX=/opt/app/tesseract/share/tessdata

# application.yml 확인
app:
  ocr:
    tessdata-path: /opt/app/tesseract/share/tessdata
```

### 권한 문제

```bash
# 디렉토리 권한 확인
ls -ld /opt/app/tesseract
ls -l /opt/app/tesseract/lib/
ls -l /opt/app/tesseract/share/tessdata/

# 권한 수정 (필요시)
sudo chown -R appuser:appuser /opt/app/tesseract
sudo chmod -R 755 /opt/app/tesseract
```

---

## 파일 크기 참고

- **tesseract-offline-ubuntu.tar.gz**: 약 15-30 MB
- **tesseract-offline-centos.tar.gz**: 약 15-30 MB
- **tesseract-bundle.tar.gz**: 약 20-40 MB
- **spring-board-image.tar.gz**: 약 300-500 MB (Docker 이미지)

---

## 권장 방법 순위

1. **Docker 이미지 방식** - 가장 간편하고 안정적
2. **번들 방식** - Docker를 사용할 수 없을 때
3. **시스템 패키지 설치** - 패키지 관리가 필요할 때

---

## 체크리스트

배포 전 확인사항:

- [ ] 운영 서버 OS 버전 확인 (Ubuntu/CentOS, 버전)
- [ ] 운영 서버 아키텍처 확인 (x86_64)
- [ ] Java 17 설치 확인
- [ ] Tesseract 패키지 또는 번들 준비
- [ ] 언어 데이터 파일 포함 확인 (kor.traineddata, eng.traineddata)
- [ ] application.yml 경로 설정 확인
- [ ] 시작 스크립트 작성
- [ ] 테스트 이미지로 OCR 동작 확인

---

## 참고 자료

- Tesseract 다운로드: https://github.com/tesseract-ocr/tesseract
- 언어 데이터: https://github.com/tesseract-ocr/tessdata
- 설치 가이드: https://tesseract-ocr.github.io/tessdoc/Installation.html
