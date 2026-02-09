package com.deepcode.springboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OCR 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ocr")
public class OcrProperties {

    /**
     * OCR 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * Tesseract 네이티브 라이브러리 경로
     * 예: /opt/homebrew/opt/tesseract/lib (macOS Homebrew)
     *     /usr/lib (Linux)
     */
    private String tesseractLibPath = "/opt/homebrew/opt/tesseract/lib";

    /**
     * Tesseract 언어 데이터 경로
     * 예: /opt/homebrew/share/tessdata (macOS Homebrew)
     *     /usr/share/tesseract-ocr/4.00/tessdata (Linux)
     */
    private String tessdataPath = "/opt/homebrew/share/tessdata";

    /**
     * OCR 언어 설정
     * 예: kor (한국어), eng (영어), kor+eng (한국어+영어)
     */
    private String language = "kor+eng";

    /**
     * OCR 엔진 모드
     * 0: Legacy engine only
     * 1: Neural nets LSTM engine only (권장)
     * 2: Legacy + LSTM engines
     * 3: Default, based on what is available
     */
    private int engineMode = 1;

    /**
     * 페이지 세그멘테이션 모드
     * 0: Orientation and script detection (OSD) only
     * 1: Automatic page segmentation with OSD
     * 3: Fully automatic page segmentation, but no OSD (권장)
     * 6: Assume a single uniform block of text
     * 11: Sparse text. Find as much text as possible in no particular order
     */
    private int pageSegMode = 3;
}
