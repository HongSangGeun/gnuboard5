# 이미지 OCR 텍스트 추출 기능 설정 가이드

## 📋 개요

게시글 작성 시 이미지를 첨부하면 자동으로 이미지 내 텍스트를 인식(OCR)하여 데이터베이스에 저장합니다.
추출된 텍스트는 검색 기능에 활용할 수 있습니다.

**⚠️ 중요**: OCR은 선택적 기능입니다. Tesseract가 설치되지 않아도 애플리케이션은 정상 작동하며, OCR 기능만 비활성화됩니다.

## 🔧 구현 내용

### 1. 기능 동작 방식
- **자동 실행**: 이미지 파일 업로드 시 자동으로 OCR 실행
- **지원 형식**: JPG, JPEG, PNG, GIF, BMP, TIFF, WEBP
- **언어 지원**: 한글 + 영어 동시 인식
- **비동기 처리**: OCR 실패 시에도 파일 업로드는 정상 처리

### 2. 코드 변경사항
- ✅ `OcrService`: Tesseract OCR 래퍼 서비스
- ✅ `BoardFile`: `bf_ocr_text` 필드 추가
- ✅ `BoardService`: 파일 저장 시 자동 OCR 실행
- ✅ `BoardFileMapper`: INSERT 쿼리에 OCR 텍스트 포함
- ✅ `gnuboard5.sql`: 테이블 스키마 업데이트

## 📌 Tesseract가 설치되지 않은 경우

애플리케이션 시작 시 다음과 같은 로그가 표시됩니다:

```
WARN  - Tesseract native library not found. OCR functionality will be disabled.
WARN  - To enable OCR on macOS, install Tesseract: brew install tesseract tesseract-lang
```

이 경우:
- ✅ **애플리케이션은 정상 작동**합니다
- ✅ **파일 업로드는 정상 작동**합니다
- ❌ **OCR 텍스트 추출만 비활성화**됩니다

OCR 기능을 사용하려면 아래 설치 방법을 따라주세요.

## 🚀 설치 방법

### macOS (Homebrew)

```bash
# Tesseract 설치
brew install tesseract

# 한글 언어 데이터 설치
brew install tesseract-lang

# 설치 확인
tesseract --version
tesseract --list-langs  # kor, eng가 있어야 함
```

### Ubuntu/Debian

```bash
# Tesseract 설치
sudo apt-get update
sudo apt-get install -y tesseract-ocr

# 한글 언어 데이터 설치
sudo apt-get install -y tesseract-ocr-kor

# 설치 확인
tesseract --version
tesseract --list-langs
```

### CentOS/RHEL

```bash
# EPEL 저장소 추가
sudo yum install -y epel-release

# Tesseract 설치
sudo yum install -y tesseract

# 한글 언어 데이터 설치
sudo yum install -y tesseract-langpack-kor

# 설치 확인
tesseract --version
tesseract --list-langs
```

### Windows

1. **Tesseract 다운로드**
   - https://github.com/UB-Mannheim/tesseract/wiki
   - Windows installer 다운로드 및 설치

2. **환경 변수 설정**
   ```cmd
   setx TESSDATA_PREFIX "C:\Program Files\Tesseract-OCR\tessdata"
   ```

3. **한글 데이터 다운로드**
   - https://github.com/tesseract-ocr/tessdata
   - `kor.traineddata` 다운로드
   - `C:\Program Files\Tesseract-OCR\tessdata` 에 복사

## 📊 데이터베이스 마이그레이션

### 방법 1: 관리자 메뉴 (권장)

1. 관리자 로그인
2. **[관리자] → [환경설정] → [DB 업그레이드]**
3. **[업그레이드 실행]** 클릭
4. `g5_board_file` 테이블에 `bf_ocr_text` 컬럼 자동 추가

### 방법 2: 수동 SQL 실행

```bash
# 모든 board_file 테이블에 OCR 컬럼 추가
mysql -u [사용자명] -p [데이터베이스명] < sql/add_ocr_text_column.sql
```

또는 직접 실행:

```sql
-- 기본 파일 테이블
ALTER TABLE g5_board_file
ADD COLUMN IF NOT EXISTS bf_ocr_text TEXT NULL COMMENT '이미지 OCR 추출 텍스트'
AFTER bf_datetime;

-- 각 게시판별 파일 테이블 (예시)
ALTER TABLE g5_board_file_notice
ADD COLUMN IF NOT EXISTS bf_ocr_text TEXT NULL;
```

## ✅ 동작 확인

### 1. 애플리케이션 로그 확인

```bash
# 애플리케이션 시작 시
OCR Service initialized with language: kor+eng
Found tessdata at: /usr/share/tessdata
```

### 2. 이미지 업로드 테스트

1. 게시판에서 텍스트가 포함된 이미지 업로드
2. 로그에서 OCR 실행 확인:
   ```
   Starting OCR for file: image.jpg
   OCR completed in 1234ms, extracted 56 characters from image.jpg
   ```

### 3. 데이터베이스 확인

```sql
-- OCR 텍스트가 저장되었는지 확인
SELECT bf_source, LENGTH(bf_ocr_text) as text_length, bf_ocr_text
FROM g5_board_file
WHERE bf_ocr_text IS NOT NULL
LIMIT 10;
```

## 🔍 검색 기능 확장 (선택사항)

OCR 텍스트를 검색에 포함하려면 검색 쿼리를 수정해야 합니다:

```java
// 예시: SearchService에서
String query = """
    SELECT w.* FROM g5_write_xxx w
    LEFT JOIN g5_board_file f ON w.wr_id = f.wr_id
    WHERE w.wr_subject LIKE ? OR w.wr_content LIKE ?
       OR f.bf_ocr_text LIKE ?
""";
```

## ⚙️ 환경 변수 설정 (선택사항)

Tesseract 데이터 경로가 자동 감지되지 않는 경우:

```bash
# Linux/macOS
export TESSDATA_PREFIX=/usr/share/tessdata

# 또는 애플리케이션 실행 시
java -DTESSDATA_PREFIX=/usr/share/tessdata -jar app.jar
```

## 🐛 문제 해결

### OCR이 실행되지 않는 경우

1. **Tesseract 설치 확인**
   ```bash
   which tesseract  # macOS/Linux
   tesseract --version
   ```

2. **한글 데이터 확인**
   ```bash
   tesseract --list-langs | grep kor
   ```

3. **로그 확인**
   ```bash
   tail -f logs/spring-boot-application.log | grep OCR
   ```

### 인식률이 낮은 경우

- **이미지 품질 개선**: 해상도가 높은 이미지 사용
- **전처리 추가**: 이미지 밝기/대비 조정 (OcrService 수정)
- **Naver Clova OCR 사용**: 더 높은 인식률 원하면 유료 API 사용

### 성능 이슈

OCR 처리 시간이 긴 경우:
- 백그라운드 작업으로 변경
- 비동기 처리 구현
- 큐(Queue) 시스템 도입

## 📈 성능 특성

- **처리 시간**: 이미지당 1-3초 (이미지 크기에 따라 다름)
- **메모리**: 이미지당 약 50-100MB 사용
- **CPU**: OCR 실행 중 높은 CPU 사용

## 🔐 보안 고려사항

- ✅ 허용된 이미지 형식만 처리
- ✅ 파일 크기 제한 적용 (10MB)
- ✅ OCR 실패 시에도 정상 동작
- ✅ SQL Injection 방지 (파라미터 바인딩)

## 📚 참고 자료

- **Tesseract OCR**: https://github.com/tesseract-ocr/tesseract
- **Tess4J (Java Wrapper)**: https://github.com/nguyenq/tess4j
- **한글 학습 데이터**: https://github.com/tesseract-ocr/tessdata

## 💡 향후 개선 사항

1. **비동기 처리**: 대용량 이미지 처리 시 성능 개선
2. **진행 상태 표시**: 사용자에게 OCR 진행 상태 피드백
3. **텍스트 보정**: 인식된 텍스트 후처리로 정확도 향상
4. **다국어 지원**: 추가 언어 팩 설정 옵션
5. **관리자 도구**: 기존 이미지 일괄 OCR 처리 기능

## ✨ 사용 예시

### 업로드 전
```
[이미지 파일: contract.jpg]
(이미지 내용: "계약서\n계약 일자: 2024-01-15\n계약 금액: 1,000,000원")
```

### 업로드 후 (자동 OCR)
```sql
SELECT bf_ocr_text FROM g5_board_file WHERE bf_file = 'xxxx.jpg';
-- 결과: "계약서\n계약 일자: 2024-01-15\n계약 금액: 1,000,000원"
```

### 검색 가능
```
검색어: "계약 일자" → 해당 이미지를 첨부한 게시글 검색 가능!
```

---

**문의사항**이 있으시면 Issues에 등록해 주세요!
