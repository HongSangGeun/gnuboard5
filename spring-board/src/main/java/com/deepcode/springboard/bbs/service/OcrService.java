package com.deepcode.springboard.bbs.service;

import com.deepcode.springboard.config.OcrProperties;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * OCR(Optical Character Recognition) 서비스
 * 이미지에서 텍스트를 추출합니다.
 * Tesseract 라이브러리가 클래스패스에 있을 때만 활성화됩니다.
 */
@Slf4j
@Service
@ConditionalOnClass(Tesseract.class)
public class OcrService {

    private final Tesseract tesseract;
    private final boolean available;
    private final OcrProperties ocrProperties;

    public OcrService(OcrProperties ocrProperties) {
        this.ocrProperties = ocrProperties;
        Tesseract temp = null;
        boolean isAvailable = false;

        if (!ocrProperties.isEnabled()) {
            log.info("OCR is disabled in configuration");
            this.tesseract = null;
            this.available = false;
            return;
        }

        try {
            // JNA library path 설정
            String jnaLibraryPath = System.getProperty("jna.library.path");
            String configuredLibPath = ocrProperties.getTesseractLibPath();

            if (configuredLibPath != null && !configuredLibPath.isEmpty()) {
                if (jnaLibraryPath == null || jnaLibraryPath.isEmpty()) {
                    System.setProperty("jna.library.path", configuredLibPath);
                    log.info("JNA library path set to: {}", configuredLibPath);
                } else if (!jnaLibraryPath.contains(configuredLibPath)) {
                    System.setProperty("jna.library.path", jnaLibraryPath + File.pathSeparator + configuredLibPath);
                    log.info("Added configured path to JNA library path: {}", configuredLibPath);
                }
            }

            temp = new Tesseract();

            // Tesseract 데이터 경로 설정
            String tessDataPath = ocrProperties.getTessdataPath();

            // 환경 변수 우선 확인
            String envTessDataPath = System.getenv("TESSDATA_PREFIX");
            if (envTessDataPath != null && !envTessDataPath.isEmpty()) {
                tessDataPath = envTessDataPath;
                log.info("Using TESSDATA_PREFIX from environment: {}", tessDataPath);
            }

            // 설정된 경로 확인
            if (tessDataPath != null && !tessDataPath.isEmpty()) {
                File tessDataDir = new File(tessDataPath);
                if (tessDataDir.exists() && tessDataDir.isDirectory()) {
                    temp.setDatapath(tessDataPath);
                    log.info("Tessdata path set to: {}", tessDataPath);
                } else {
                    log.warn("Configured tessdata path does not exist: {}", tessDataPath);
                    // 기본 경로 시도
                    tessDataPath = tryDefaultPaths();
                    if (tessDataPath != null) {
                        temp.setDatapath(tessDataPath);
                    }
                }
            } else {
                // 기본 경로 시도
                tessDataPath = tryDefaultPaths();
                if (tessDataPath != null) {
                    temp.setDatapath(tessDataPath);
                }
            }

            // 언어 설정
            temp.setLanguage(ocrProperties.getLanguage());
            log.info("OCR language set to: {}", ocrProperties.getLanguage());

            // OCR 엔진 모드
            temp.setOcrEngineMode(ocrProperties.getEngineMode());

            // 페이지 세그멘테이션 모드
            temp.setPageSegMode(ocrProperties.getPageSegMode());

            // 실제 네이티브 라이브러리 로딩 테스트
            try {
                BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
                temp.doOCR(testImage);
                isAvailable = true;
                log.info("OCR Service initialized successfully");
            } catch (UnsatisfiedLinkError e) {
                throw e;
            } catch (Exception e) {
                // 테스트 실패는 무시 (빈 이미지라 실패할 수 있음)
                isAvailable = true;
                log.info("OCR Service initialized (test skipped)");
            }

        } catch (UnsatisfiedLinkError e) {
            log.warn("Tesseract native library not found. OCR functionality will be disabled.");
            log.warn("To enable OCR:");
            log.warn("  macOS: brew install tesseract tesseract-lang");
            log.warn("  Linux: apt-get install tesseract-ocr tesseract-ocr-kor");
            log.warn("Configure paths in application.yml under app.ocr");
        } catch (Exception e) {
            log.error("Failed to initialize Tesseract OCR: {}", e.getMessage());
        }

        this.tesseract = temp;
        this.available = isAvailable;
    }

    private String tryDefaultPaths() {
        String[] possiblePaths = {
            "/opt/homebrew/share/tessdata",
            "/usr/share/tesseract-ocr/4.00/tessdata",
            "/usr/share/tessdata",
            "./tessdata"
        };

        for (String path : possiblePaths) {
            File tessDataDir = new File(path);
            if (tessDataDir.exists() && tessDataDir.isDirectory()) {
                log.info("Found tessdata at default path: {}", path);
                return path;
            }
        }
        log.warn("Could not find tessdata in any default paths");
        return null;
    }

    /**
     * 이미지 파일에서 텍스트 추출
     *
     * @param imageFile 이미지 파일
     * @return 추출된 텍스트 (실패 시 null)
     */
    public String extractText(File imageFile) {
        if (!available || tesseract == null) {
            log.debug("OCR is not available");
            return null;
        }

        if (imageFile == null || !imageFile.exists()) {
            log.warn("Image file is null or does not exist");
            return null;
        }

        try {
            // 이미지 파일 확인
            String fileName = imageFile.getName().toLowerCase();
            if (!isImageFile(fileName)) {
                log.debug("Not an image file: {}", fileName);
                return null;
            }

            log.info("Starting OCR for file: {}", imageFile.getName());
            long startTime = System.currentTimeMillis();

            // 이미지 로드
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                log.warn("Failed to load image: {}", imageFile.getName());
                return null;
            }

            // OCR 실행
            String text = tesseract.doOCR(image);

            long duration = System.currentTimeMillis() - startTime;

            if (text != null && !text.trim().isEmpty()) {
                log.info("OCR completed in {}ms, extracted {} characters from {}",
                        duration, text.length(), imageFile.getName());
                return text.trim();
            } else {
                log.info("No text found in image: {}", imageFile.getName());
                return null;
            }

        } catch (UnsatisfiedLinkError e) {
            log.error("Tesseract native library not found. Skipping OCR for file: {}", imageFile.getName());
            return null;
        } catch (TesseractException e) {
            log.error("Tesseract OCR error for file {}: {}", imageFile.getName(), e.getMessage());
            return null;
        } catch (IOException e) {
            log.error("Failed to read image file {}: {}", imageFile.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during OCR for file {}: {}", imageFile.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 파일 경로로 텍스트 추출
     *
     * @param filePath 이미지 파일 경로
     * @return 추출된 텍스트 (실패 시 null)
     */
    public String extractText(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return extractText(new File(filePath));
    }

    /**
     * BufferedImage에서 텍스트 추출 (PDF 렌더링 이미지 등)
     *
     * @param image BufferedImage 객체
     * @return 추출된 텍스트 (실패 시 null)
     */
    public String extractText(BufferedImage image) {
        if (!available || tesseract == null) {
            log.debug("OCR is not available");
            return null;
        }

        if (image == null) {
            log.warn("BufferedImage is null");
            return null;
        }

        try {
            String text = tesseract.doOCR(image);

            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            } else {
                return null;
            }

        } catch (UnsatisfiedLinkError e) {
            log.error("Tesseract native library not found");
            return null;
        } catch (TesseractException e) {
            log.error("Tesseract OCR error: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during OCR: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 이미지 파일 여부 확인
     *
     * @param fileName 파일명
     * @return 이미지 파일이면 true
     */
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

    /**
     * OCR 가능 여부 확인
     * Tesseract가 올바르게 설정되었는지 확인
     *
     * @return OCR 사용 가능하면 true
     */
    public boolean isOcrAvailable() {
        return available && tesseract != null;
    }
}
