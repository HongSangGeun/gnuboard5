package com.deepcode.springboard.bbs.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * PDF 텍스트 추출 서비스
 * 1. 텍스트 기반 PDF: 직접 텍스트 추출
 * 2. 이미지 기반 PDF: 이미지로 렌더링 후 OCR
 */
@Slf4j
@Service
public class PdfService {

    private final OcrService ocrService;

    public PdfService(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    /**
     * PDF 파일에서 텍스트 추출
     *
     * @param pdfFile PDF 파일
     * @return 추출된 텍스트 (실패 시 null)
     */
    public String extractText(File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) {
            log.warn("PDF file is null or does not exist");
            return null;
        }

        if (!isPdfFile(pdfFile.getName())) {
            log.debug("Not a PDF file: {}", pdfFile.getName());
            return null;
        }

        log.info("Starting PDF text extraction for: {}", pdfFile.getName());
        long startTime = System.currentTimeMillis();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            // 1단계: 직접 텍스트 추출 시도
            String text = extractTextDirectly(document, pdfFile.getName());

            // 텍스트가 충분히 추출되었으면 반환
            if (text != null && text.trim().length() > 50) {
                long duration = System.currentTimeMillis() - startTime;
                log.info("PDF text extraction completed in {}ms, extracted {} characters from {}",
                        duration, text.length(), pdfFile.getName());
                return text.trim();
            }

            // 2단계: 텍스트가 부족하면 OCR 시도 (이미지 기반 PDF일 가능성)
            log.info("Insufficient text found, attempting OCR for: {}", pdfFile.getName());
            text = extractTextWithOcr(document, pdfFile.getName());

            long duration = System.currentTimeMillis() - startTime;
            if (text != null && !text.trim().isEmpty()) {
                log.info("PDF OCR extraction completed in {}ms, extracted {} characters from {}",
                        duration, text.length(), pdfFile.getName());
                return text.trim();
            } else {
                log.info("No text found in PDF: {}", pdfFile.getName());
                return null;
            }

        } catch (IOException e) {
            log.error("Failed to process PDF file {}: {}", pdfFile.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during PDF processing for {}: {}", pdfFile.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * PDF에서 직접 텍스트 추출 (텍스트 기반 PDF용)
     * 페이지별로 구분하여 추출
     */
    private String extractTextDirectly(PDDocument document, String fileName) {
        try {
            int pageCount = document.getNumberOfPages();
            StringBuilder allText = new StringBuilder();
            PDFTextStripper stripper = new PDFTextStripper();

            // 페이지별로 텍스트 추출
            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);

                if (pageText != null && !pageText.trim().isEmpty()) {
                    allText.append("--- Page ").append(i).append(" ---\n");
                    allText.append(pageText.trim()).append("\n\n");
                }
            }

            if (allText.length() > 0) {
                log.debug("Direct text extraction successful for: {} ({} pages)", fileName, pageCount);
                return allText.toString();
            }
        } catch (IOException e) {
            log.warn("Direct text extraction failed for {}: {}", fileName, e.getMessage());
        }
        return null;
    }

    /**
     * PDF를 이미지로 렌더링 후 OCR (이미지 기반 PDF용)
     */
    private String extractTextWithOcr(PDDocument document, String fileName) {
        if (!ocrService.isOcrAvailable()) {
            log.warn("OCR is not available, cannot extract text from image-based PDF");
            return null;
        }

        StringBuilder allText = new StringBuilder();
        PDFRenderer renderer = new PDFRenderer(document);
        int pageCount = document.getNumberOfPages();

        // 최대 10페이지까지만 처리 (성능 고려)
        int maxPages = Math.min(pageCount, 10);

        log.info("Processing {} pages with OCR for: {}", maxPages, fileName);

        for (int pageIndex = 0; pageIndex < maxPages; pageIndex++) {
            try {
                // PDF 페이지를 이미지로 렌더링 (300 DPI)
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300);

                // OCR로 텍스트 추출
                String pageText = ocrService.extractText(image);

                if (pageText != null && !pageText.trim().isEmpty()) {
                    allText.append("--- Page ").append(pageIndex + 1).append(" ---\n");
                    allText.append(pageText).append("\n\n");
                }

                log.debug("OCR completed for page {} of {}", pageIndex + 1, fileName);

            } catch (IOException e) {
                log.warn("Failed to render page {} of {}: {}", pageIndex + 1, fileName, e.getMessage());
            } catch (Exception e) {
                log.error("OCR error on page {} of {}: {}", pageIndex + 1, fileName, e.getMessage());
            }
        }

        if (pageCount > maxPages) {
            log.info("Processed only {} of {} pages for performance reasons", maxPages, pageCount);
        }

        return allText.length() > 0 ? allText.toString() : null;
    }

    /**
     * PDF 파일 여부 확인
     */
    private boolean isPdfFile(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    /**
     * 파일 경로로 텍스트 추출
     *
     * @param filePath PDF 파일 경로
     * @return 추출된 텍스트 (실패 시 null)
     */
    public String extractText(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return extractText(new File(filePath));
    }
}
