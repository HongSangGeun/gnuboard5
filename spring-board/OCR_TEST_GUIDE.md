# OCR 기능 테스트 가이드

## ✅ 사전 확인 완료
- ✅ Tesseract 5.5.0 설치됨
- ✅ 한글(kor) + 영어(eng) 언어팩 설치됨
- ✅ `bf_ocr_text` 컬럼 추가됨 (g5_board_file)

---

## 🧪 테스트 방법

### **방법 1: 웹 UI에서 테스트 (권장)**

#### 1️⃣ 테스트용 이미지 준비

아래 명령으로 간단한 테스트 이미지를 생성하거나, 텍스트가 포함된 스크린샷을 사용하세요:

```bash
# 스크린샷을 찍거나 다운로드
# 예: 명함, 문서 사진, 간판 사진 등
```

**권장 테스트 이미지:**
- 명함 사진
- 책 페이지 스크린샷
- 한글/영어가 섞인 문서
- 간판이나 표지판 사진

#### 2️⃣ 애플리케이션 시작

```bash
cd /Users/sanggeunhong/project/gnuboard5/spring-board
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./gradlew bootRun
```

또는 IntelliJ에서 실행:
```
SpringBoardApplication 실행
```

#### 3️⃣ 게시글 작성 및 이미지 업로드

1. 브라우저에서 게시판 접속
   ```
   http://localhost:8080
   ```

2. 로그인 (관리자 또는 일반 회원)

3. 게시판 선택 (예: 갤러리, 자유게시판)

4. 새 게시글 작성
   - 제목: "OCR 테스트"
   - 내용: "이미지에서 텍스트를 추출합니다"
   - **파일 첨부**: 텍스트가 포함된 이미지 업로드

5. 저장 버튼 클릭

#### 4️⃣ 로그 확인

터미널에서 애플리케이션 로그를 확인:

**✅ OCR 성공 시:**
```
INFO  - OCR Service initialized successfully with language: kor+eng
INFO  - Found tessdata at: /opt/homebrew/share/tessdata
INFO  - Starting OCR for file: test_image.jpg
INFO  - OCR completed in 1234ms, extracted 56 characters from test_image.jpg
```

**❌ OCR 비활성 시:**
```
WARN  - Tesseract native library not found. OCR functionality will be disabled.
```

#### 5️⃣ 데이터베이스 확인

MySQL에서 OCR 결과 확인:

```sql
-- 최근 업로드된 파일의 OCR 텍스트 확인
SELECT
    bf_no,
    bf_source AS '원본파일명',
    LENGTH(bf_ocr_text) AS '텍스트길이',
    SUBSTRING(bf_ocr_text, 1, 100) AS 'OCR결과미리보기',
    bf_datetime AS '업로드일시'
FROM g5_board_file
WHERE bf_ocr_text IS NOT NULL
ORDER BY bf_datetime DESC
LIMIT 5;
```

**전체 OCR 텍스트 보기:**
```sql
SELECT
    bf_source AS '파일명',
    bf_ocr_text AS 'OCR추출텍스트'
FROM g5_board_file
WHERE bf_no = 0  -- 첫 번째 첨부파일
  AND bo_table = 'gallery'  -- 게시판 ID
  AND wr_id = 1;  -- 게시글 번호
```

---

### **방법 2: 직접 테스트 이미지 생성**

#### 간단한 텍스트 이미지 생성 (macOS)

```bash
# ImageMagick 설치 (선택사항)
brew install imagemagick

# 테스트 이미지 생성
convert -size 800x400 xc:white \
  -font Arial \
  -pointsize 40 \
  -fill black \
  -gravity center \
  -annotate +0+0 "OCR 테스트\nHello World\n안녕하세요" \
  /tmp/ocr_test.png

# 생성된 이미지 확인
open /tmp/ocr_test.png
```

이 이미지를 게시판에 업로드하면:
```
예상 OCR 결과:
OCR 테스트
Hello World
안녕하세요
```

---

### **방법 3: 커맨드라인으로 직접 테스트**

Tesseract를 직접 실행해서 테스트:

```bash
# 테스트 이미지로 OCR 실행
tesseract /tmp/ocr_test.png stdout -l kor+eng

# 결과를 파일로 저장
tesseract /tmp/ocr_test.png /tmp/result -l kor+eng
cat /tmp/result.txt
```

---

## 📊 예상 결과

### ✅ 성공 케이스

**1. 로그 출력:**
```
INFO  - Starting OCR for file: 명함.jpg
INFO  - OCR completed in 2341ms, extracted 128 characters from 명함.jpg
```

**2. 데이터베이스:**
```
+-------+---------------+--------------+---------------------------+---------------------+
| bf_no | 원본파일명    | 텍스트길이   | OCR결과미리보기           | 업로드일시          |
+-------+---------------+--------------+---------------------------+---------------------+
|     0 | 명함.jpg      |          128 | 홍길동\n010-1234-5678... | 2026-02-09 02:30:00 |
+-------+---------------+--------------+---------------------------+---------------------+
```

### ⚠️ 실패 케이스

**케이스 1: Tesseract 미설치**
```
WARN  - Tesseract native library not found. OCR functionality will be disabled.
```
👉 해결: `brew install tesseract tesseract-lang`

**케이스 2: 이미지가 아닌 파일**
```
DEBUG - Not an image file: document.pdf
```
👉 정상: PDF는 OCR 대상이 아님

**케이스 3: 텍스트 없는 이미지**
```
INFO  - No text found in image: landscape.jpg
```
👉 정상: 풍경 사진 등에는 텍스트가 없을 수 있음

**케이스 4: OCR 처리 실패**
```
ERROR - Tesseract OCR error for file image.jpg: ...
```
👉 확인: 이미지 파일 손상 여부, 형식 지원 여부

---

## 🔍 상세 디버깅

### 애플리케이션 로그 레벨 조정

`application.yml` 또는 `application.properties`에 추가:

```yaml
logging:
  level:
    com.gnuboard.springboard.bbs.service.OcrService: DEBUG
    com.gnuboard.springboard.bbs.service.BoardService: DEBUG
```

### OCR 처리 시간 확인

로그에서 처리 시간을 확인할 수 있습니다:
```
OCR completed in 1234ms, extracted 56 characters from image.jpg
```

- **1-3초**: 정상 (작은 이미지)
- **3-10초**: 정상 (큰 이미지 또는 복잡한 텍스트)
- **10초 이상**: 이미지 크기 확인 필요

### 지원되는 이미지 형식

```java
// OcrService.java에서 확인
private boolean isImageFile(String fileName) {
    String lowerCase = fileName.toLowerCase();
    return lowerCase.endsWith(".jpg") ||
           lowerCase.endsWith(".jpeg") ||
           lowerCase.endsWith(".png") ||
           lowerCase.endsWith(".gif") ||
           lowerCase.endsWith(".bmp") ||
           lowerCase.endsWith(".tiff") ||
           lowerCase.endsWith(".tif");
}
```

---

## 📝 트러블슈팅

### Q1: "OCR이 실행되지 않아요"

**체크리스트:**
1. ✅ Tesseract 설치 확인: `tesseract --version`
2. ✅ 한글 언어팩 확인: `tesseract --list-langs | grep kor`
3. ✅ 애플리케이션 로그 확인: OCR Service 초기화 메시지 확인
4. ✅ 이미지 형식 확인: JPG, PNG 등 지원 형식인지
5. ✅ `bf_ocr_text` 컬럼 존재 확인: `DESCRIBE g5_board_file;`

### Q2: "인식률이 낮아요"

**개선 방법:**
- 고해상도 이미지 사용 (최소 300 DPI)
- 명확한 폰트 사용 (손글씨는 인식률 낮음)
- 배경이 단순한 이미지
- 텍스트가 수평으로 정렬된 이미지

### Q3: "처리 속도가 느려요"

**최적화 방법:**
- 이미지 크기 조정 (최대 2000x2000 픽셀)
- 비동기 처리 구현 (향후 개선)
- 큐 시스템 도입 (대량 처리 시)

---

## 🎯 실전 활용 예시

### 예시 1: 명함 관리
```
1. 명함 사진 촬영
2. 게시판 업로드
3. OCR로 자동 텍스트 추출
4. 이름, 전화번호, 이메일 검색 가능
```

### 예시 2: 문서 아카이빙
```
1. 종이 문서 스캔 또는 촬영
2. 게시판에 보관
3. OCR로 내용 검색 가능
4. 원본 이미지는 그대로 보존
```

### 예시 3: 간판/표지판 기록
```
1. 간판 사진 업로드
2. 상호명, 전화번호 자동 추출
3. 지역별, 업종별 검색 가능
```

---

## ✅ 최종 확인 체크리스트

- [ ] Tesseract 설치 확인
- [ ] 한글 언어팩 설치 확인
- [ ] 애플리케이션 실행
- [ ] 로그에서 OCR 초기화 메시지 확인
- [ ] 테스트 이미지 업로드
- [ ] 로그에서 OCR 실행 메시지 확인
- [ ] 데이터베이스에서 결과 확인
- [ ] 웹 UI에서 정상 동작 확인

---

**모든 체크리스트 통과 시: 🎉 OCR 기능이 정상 작동하고 있습니다!**
