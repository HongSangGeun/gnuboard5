# OCR 설정 가이드

## 개요

Tesseract OCR 설정을 `application.yml` 파일에서 관리할 수 있습니다.

## 설정 항목

### application.yml

```yaml
app:
  ocr:
    # OCR 기능 활성화 여부
    enabled: true

    # Tesseract 네이티브 라이브러리 경로
    tesseract-lib-path: /opt/homebrew/opt/tesseract/lib

    # Tesseract 언어 데이터 경로
    tessdata-path: /opt/homebrew/share/tessdata

    # OCR 언어 (kor: 한국어, eng: 영어, kor+eng: 한국어+영어)
    language: kor+eng

    # OCR 엔진 모드
    # 0: Legacy engine only
    # 1: Neural nets LSTM engine only (권장)
    # 2: Legacy + LSTM engines
    # 3: Default, based on what is available
    engine-mode: 1

    # 페이지 세그멘테이션 모드
    # 0: Orientation and script detection (OSD) only
    # 1: Automatic page segmentation with OSD
    # 3: Fully automatic page segmentation, but no OSD (권장)
    # 6: Assume a single uniform block of text
    # 11: Sparse text. Find as much text as possible
    page-seg-mode: 3
```

## 환경 변수로 설정

환경 변수를 사용하여 설정을 오버라이드할 수 있습니다:

```bash
# OCR 활성화/비활성화
export OCR_ENABLED=true

# Tesseract 라이브러리 경로
export TESSERACT_LIB_PATH=/opt/homebrew/opt/tesseract/lib

# Tessdata 경로
export TESSDATA_PATH=/opt/homebrew/share/tessdata

# OCR 언어
export OCR_LANGUAGE=kor+eng

# 엔진 모드
export OCR_ENGINE_MODE=1

# 페이지 세그멘테이션 모드
export OCR_PAGE_SEG_MODE=3
```

## 플랫폼별 기본 경로

### macOS (Homebrew)

```yaml
app:
  ocr:
    tesseract-lib-path: /opt/homebrew/opt/tesseract/lib
    tessdata-path: /opt/homebrew/share/tessdata
```

**설치 명령:**
```bash
brew install tesseract tesseract-lang
```

### Ubuntu/Debian Linux

```yaml
app:
  ocr:
    tesseract-lib-path: /usr/lib
    tessdata-path: /usr/share/tesseract-ocr/4.00/tessdata
```

**설치 명령:**
```bash
sudo apt-get update
sudo apt-get install tesseract-ocr tesseract-ocr-kor tesseract-ocr-eng
```

### CentOS/RHEL Linux

```yaml
app:
  ocr:
    tesseract-lib-path: /usr/lib64
    tessdata-path: /usr/share/tessdata
```

**설치 명령:**
```bash
sudo yum install tesseract tesseract-langpack-kor tesseract-langpack-eng
```

## OCR 비활성화

OCR 기능을 비활성화하려면:

```yaml
app:
  ocr:
    enabled: false
```

또는 환경 변수:
```bash
export OCR_ENABLED=false
```

## 경로 자동 탐지

설정에서 경로를 지정하지 않으면 다음 경로를 자동으로 시도합니다:

**tessdata 경로:**
1. `/opt/homebrew/share/tessdata` (macOS Homebrew)
2. `/usr/share/tesseract-ocr/4.00/tessdata` (Linux)
3. `/usr/share/tessdata` (Linux)
4. `./tessdata` (현재 디렉토리)

## 언어 팩 추가

### 한국어만 사용

```yaml
app:
  ocr:
    language: kor
```

### 영어만 사용

```yaml
app:
  ocr:
    language: eng
```

### 여러 언어 사용

```yaml
app:
  ocr:
    language: kor+eng+jpn  # 한국어 + 영어 + 일본어
```

## 문제 해결

### 1. 네이티브 라이브러리를 찾을 수 없음

**증상:**
```
Tesseract native library not found. OCR functionality will be disabled.
```

**해결:**
1. Tesseract가 설치되어 있는지 확인:
   ```bash
   tesseract --version
   ```

2. 라이브러리 경로 확인:
   - macOS: `ls /opt/homebrew/opt/tesseract/lib/libtesseract.dylib`
   - Linux: `ls /usr/lib/libtesseract.so` 또는 `ls /usr/lib64/libtesseract.so`

3. `application.yml`에서 올바른 경로 설정

### 2. 언어 데이터를 찾을 수 없음

**증상:**
```
Could not find tessdata in any default paths
```

**해결:**
1. tessdata 디렉토리 확인:
   ```bash
   # macOS
   ls /opt/homebrew/share/tessdata/kor.traineddata

   # Linux
   ls /usr/share/tesseract-ocr/4.00/tessdata/kor.traineddata
   ```

2. 언어 팩 설치:
   - macOS: `brew install tesseract-lang`
   - Ubuntu: `sudo apt-get install tesseract-ocr-kor`

3. `application.yml`에서 올바른 경로 설정

### 3. OCR이 작동하지 않음

1. 애플리케이션 로그 확인:
   ```
   OCR Service initialized successfully
   ```

2. 진단 정보 확인 (시작 시 출력):
   ```
   ✅ OCR 기능이 정상적으로 작동합니다!
   ```

3. 설정 확인:
   ```yaml
   app:
     ocr:
       enabled: true  # false가 아닌지 확인
   ```

## 성능 튜닝

### 엔진 모드 최적화

- **빠른 처리 (정확도 낮음)**: `engine-mode: 0`
- **균형 (권장)**: `engine-mode: 1`
- **정확도 우선 (느림)**: `engine-mode: 2`

### 페이지 세그멘테이션 최적화

- **일반 문서**: `page-seg-mode: 3`
- **단일 텍스트 블록**: `page-seg-mode: 6`
- **희소 텍스트 (스크린샷)**: `page-seg-mode: 11`

## 예제 설정

### 개발 환경 (macOS)

```yaml
app:
  ocr:
    enabled: true
    tesseract-lib-path: /opt/homebrew/opt/tesseract/lib
    tessdata-path: /opt/homebrew/share/tessdata
    language: kor+eng
    engine-mode: 1
    page-seg-mode: 3
```

### 프로덕션 환경 (Linux)

```yaml
app:
  ocr:
    enabled: ${OCR_ENABLED:true}
    tesseract-lib-path: ${TESSERACT_LIB_PATH:/usr/lib}
    tessdata-path: ${TESSDATA_PATH:/usr/share/tesseract-ocr/4.00/tessdata}
    language: ${OCR_LANGUAGE:kor+eng}
    engine-mode: ${OCR_ENGINE_MODE:1}
    page-seg-mode: ${OCR_PAGE_SEG_MODE:3}
```

### Docker 환경

```yaml
app:
  ocr:
    enabled: true
    tesseract-lib-path: /usr/lib
    tessdata-path: /usr/share/tessdata
    language: kor+eng
    engine-mode: 1
    page-seg-mode: 3
```

**Dockerfile 예시:**
```dockerfile
FROM eclipse-temurin:17-jre

# Tesseract 설치
RUN apt-get update && \
    apt-get install -y tesseract-ocr tesseract-ocr-kor tesseract-ocr-eng && \
    rm -rf /var/lib/apt/lists/*

COPY target/spring-board.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]
```

## 참고

- Tesseract 공식 문서: https://tesseract-ocr.github.io/
- 언어 데이터 다운로드: https://github.com/tesseract-ocr/tessdata
