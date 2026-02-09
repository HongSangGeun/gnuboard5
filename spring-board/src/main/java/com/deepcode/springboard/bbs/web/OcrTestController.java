package com.deepcode.springboard.bbs.web;

import com.deepcode.springboard.bbs.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * OCR 기능 테스트 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/ocr-test")
@RequiredArgsConstructor
public class OcrTestController {

    private final OcrService ocrService;

    /**
     * OCR 테스트 페이지
     */
    @GetMapping
    public String testPage(Model model) {
        boolean isAvailable = ocrService.isOcrAvailable();
        model.addAttribute("ocrAvailable", isAvailable);

        log.info("=================================================");
        log.info("OCR 테스트 페이지 접근");
        log.info("OCR 사용 가능 여부: {}", isAvailable);
        log.info("=================================================");

        return "ocr-test";
    }

    /**
     * OCR 상태 확인 API
     */
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();

        boolean isAvailable = ocrService.isOcrAvailable();
        status.put("available", isAvailable);
        status.put("message", isAvailable ?
            "✅ OCR 기능이 정상적으로 작동합니다!" :
            "❌ OCR 기능이 비활성화되어 있습니다. Tesseract를 설치하세요.");

        log.info("OCR 상태 확인 요청 - 사용 가능: {}", isAvailable);

        return status;
    }

    /**
     * 이미지 업로드 및 OCR 테스트
     */
    @PostMapping("/extract")
    @ResponseBody
    public Map<String, Object> extractText(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        log.info("=================================================");
        log.info("OCR 추출 요청 - 파일: {}, 크기: {} bytes",
            file.getOriginalFilename(), file.getSize());

        try {
            if (!ocrService.isOcrAvailable()) {
                log.warn("OCR을 사용할 수 없습니다.");
                result.put("success", false);
                result.put("message", "OCR 기능이 비활성화되어 있습니다.");
                return result;
            }

            // 임시 파일 생성
            File tempFile = File.createTempFile("ocr-test-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);

            log.info("임시 파일 생성: {}", tempFile.getAbsolutePath());

            // 이미지 파일 검증
            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                log.error("이미지를 읽을 수 없습니다: {}", file.getOriginalFilename());
                result.put("success", false);
                result.put("message", "유효한 이미지 파일이 아닙니다.");
                tempFile.delete();
                return result;
            }

            log.info("이미지 크기: {}x{}", image.getWidth(), image.getHeight());

            // OCR 실행
            log.info("OCR 추출 시작...");
            long startTime = System.currentTimeMillis();

            String extractedText = ocrService.extractText(tempFile);

            long duration = System.currentTimeMillis() - startTime;

            if (extractedText != null && !extractedText.isEmpty()) {
                log.info("✅ OCR 성공! 처리 시간: {}ms, 추출된 텍스트 길이: {} 글자",
                    duration, extractedText.length());
                log.info("추출된 텍스트:\n{}", extractedText);

                result.put("success", true);
                result.put("text", extractedText);
                result.put("length", extractedText.length());
                result.put("duration", duration + "ms");
                result.put("message", "OCR 추출 성공!");
            } else {
                log.warn("⚠️ 이미지에서 텍스트를 찾지 못했습니다.");
                result.put("success", true);
                result.put("text", "");
                result.put("message", "이미지에서 텍스트를 찾지 못했습니다.");
            }

            // 임시 파일 삭제
            tempFile.delete();

        } catch (Exception e) {
            log.error("❌ OCR 처리 중 오류 발생: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "오류: " + e.getMessage());
        }

        log.info("=================================================");

        return result;
    }
}
