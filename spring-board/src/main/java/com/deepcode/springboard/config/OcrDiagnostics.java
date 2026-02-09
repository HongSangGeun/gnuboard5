package com.deepcode.springboard.config;

import com.deepcode.springboard.bbs.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * OCR 진단 도구
 */
@Slf4j
@Component
public class OcrDiagnostics implements CommandLineRunner {

    private final ApplicationContext applicationContext;

    public OcrDiagnostics(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n\n");
        System.out.println("=======================================================");
        System.out.println("           OCR 진단 시작                               ");
        System.out.println("=======================================================");

        // OcrService 빈 확인
        try {
            OcrService ocrService = applicationContext.getBean(OcrService.class);
            System.out.println("✅ OcrService 빈이 로드되었습니다!");

            boolean available = ocrService.isOcrAvailable();
            if (available) {
                System.out.println("✅ OCR 기능이 정상적으로 작동합니다!");
            } else {
                System.out.println("⚠️  OcrService는 로드되었지만 OCR이 비활성화되어 있습니다.");
                System.out.println("⚠️  Tesseract 네이티브 라이브러리가 설치되지 않았을 수 있습니다.");
            }
        } catch (Exception e) {
            System.out.println("❌ OcrService 빈을 찾을 수 없습니다!");
            System.out.println("❌ 원인: " + e.getMessage());
            System.out.println("❌ @ConditionalOnClass(Tesseract.class) 조건을 만족하지 못했을 수 있습니다.");
        }

        // Tesseract 클래스 확인
        try {
            Class.forName("net.sourceforge.tess4j.Tesseract");
            System.out.println("✅ Tesseract 클래스가 클래스패스에 있습니다.");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Tesseract 클래스를 찾을 수 없습니다!");
            System.out.println("❌ tess4j 의존성이 누락되었을 수 있습니다.");
        }

        // Tesseract 명령어 확인
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"tesseract", "--version"});
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("✅ Tesseract 명령어가 시스템에 설치되어 있습니다.");
            } else {
                System.out.println("⚠️  Tesseract 명령어 실행 실패 (종료 코드: " + exitCode + ")");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Tesseract 명령어를 찾을 수 없습니다: " + e.getMessage());
        }

        System.out.println("=======================================================");
        System.out.println("           OCR 진단 완료                               ");
        System.out.println("=======================================================");
        System.out.println("\n\n");
    }
}
