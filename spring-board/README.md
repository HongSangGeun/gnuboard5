# spring-board

그누보드5 BBS를 Spring Boot로 옮기기 위한 최소 골격입니다.

## 구성
- Spring Boot 3.2 (Java 17)
- MyBatis + JDBC
- Thymeleaf

## 실행 전 준비
1. MySQL에 기존 그누보드 DB가 있어야 합니다.
2. `spring-board/src/main/resources/application.yml`에서 DB 접속 정보를 수정하세요.
3. `app.g5.write-prefix`가 실제 환경의 게시글 테이블 접두사와 일치하는지 확인하세요. (기본값 `g5_write_`)
4. 첨부파일 저장 경로 `app.g5.data-path`가 실제 `data` 디렉토리를 가리키는지 확인하세요. (기본값 `../data`)

## 실행
- Gradle이 설치되어 있다면:
  - `gradle bootRun`

## 기본 URL
- 목록: `/bbs/{boTable}/list`
- 보기: `/bbs/{boTable}/view/{wrId}`
- 글쓰기: `/bbs/{boTable}/write`
- 다운로드: `/bbs/{boTable}/download/{wrId}/{bfNo}`
- 로그인: `/login`
- 로그아웃: `/logout`
- 관리자: `/mgmt/boards`

예) `/bbs/notice/list`

## 비밀번호 처리
- 회원 로그인과 비회원 글 비밀번호 모두 **그누보드 PBKDF2 해시**를 기준으로 검증합니다.
- 기존 MySQL PASSWORD 방식(`*`로 시작하는 41자 해시)도 검증 가능합니다.

## 스킨 구조
- DB의 `g5_board.bo_skin` 값에 맞춰 `templates/skin/board/{skin}/` 경로를 탐색합니다.
- 기본으로 `templates/skin/board/basic/`을 제공하며, 없으면 `templates/bbs/`를 사용합니다.
- 현재 이식된 스킨은 `basic`, `gallery`, `schedule` 입니다.

## 게시판 관리
- `/mgmt/boards`에서 게시판 생성/수정이 가능합니다.
- 관리자 계정은 `g5_config.cf_admin` 값과 로그인 ID가 일치해야 합니다.
- 관리자 화면에서 게시판 삭제 시 게시글 테이블과 첨부파일이 함께 삭제됩니다.
- 게시판 복제/이동 기능을 제공합니다. (테이블/파일 경로 변경 포함)

## 검색/정렬
- 목록에서 제목/내용/작성자 검색과 날짜 범위(`sdate`, `edate`) 필터를 제공합니다.

## 첨부파일
- 수정 화면에서 기존 파일을 선택 삭제할 수 있습니다.
