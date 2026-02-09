-- bf_ocr_text 컬럼을 TEXT에서 LONGTEXT로 변경
-- PDF 파일 등 대용량 텍스트 지원 및 이모지 등 4바이트 문자 지원을 위함

ALTER TABLE `g5_board_file`
MODIFY COLUMN `bf_ocr_text` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'OCR 추출 텍스트 (이미지/PDF)';
