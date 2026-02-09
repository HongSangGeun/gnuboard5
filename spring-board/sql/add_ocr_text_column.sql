-- 게시판 첨부파일 테이블에 OCR 텍스트 컬럼 추가
-- 모든 g5_board_file_* 테이블에 bf_ocr_text 컬럼을 추가합니다.

-- 동적으로 모든 게시판 파일 테이블에 컬럼 추가
-- 실행 전 백업 권장

-- 예시: 기본 게시판 파일 테이블
ALTER TABLE g5_board_file
ADD COLUMN IF NOT EXISTS bf_ocr_text TEXT NULL COMMENT '이미지 OCR 추출 텍스트'
AFTER bf_datetime;

-- 각 게시판별 파일 테이블도 동일하게 처리 필요
-- 예: ALTER TABLE g5_board_file_notice ADD COLUMN IF NOT EXISTS bf_ocr_text TEXT NULL;
-- 예: ALTER TABLE g5_board_file_free ADD COLUMN IF NOT EXISTS bf_ocr_text TEXT NULL;

-- 또는 모든 board_file 테이블을 자동으로 찾아서 처리하는 프로시저
DELIMITER $$

CREATE PROCEDURE AddOcrTextColumnToAllBoardFiles()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE tableName VARCHAR(255);
    DECLARE cur CURSOR FOR
        SELECT TABLE_NAME
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME LIKE 'g5_board_file%';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO tableName;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 컬럼이 이미 존재하는지 확인
        SET @columnExists = 0;
        SELECT COUNT(*) INTO @columnExists
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = 'bf_ocr_text';

        IF @columnExists = 0 THEN
            SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN bf_ocr_text TEXT NULL COMMENT ''이미지 OCR 추출 텍스트'' AFTER bf_datetime');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SELECT CONCAT('Added bf_ocr_text to ', tableName) AS result;
        ELSE
            SELECT CONCAT('bf_ocr_text already exists in ', tableName) AS result;
        END IF;
    END LOOP;

    CLOSE cur;
END$$

DELIMITER ;

-- 프로시저 실행
CALL AddOcrTextColumnToAllBoardFiles();

-- 프로시저 삭제 (선택사항)
DROP PROCEDURE IF EXISTS AddOcrTextColumnToAllBoardFiles;
