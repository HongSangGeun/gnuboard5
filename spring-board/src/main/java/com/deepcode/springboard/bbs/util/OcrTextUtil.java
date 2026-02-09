package com.deepcode.springboard.bbs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR/PDF 텍스트 처리 유틸리티
 */
public class OcrTextUtil {

    private static final Pattern PAGE_MARKER_PATTERN = Pattern.compile("--- Page (\\d+) ---");

    /**
     * 검색어가 포함된 페이지 번호 추출
     *
     * @param ocrText OCR 추출 텍스트 (페이지 마커 포함)
     * @param searchTerm 검색어
     * @return 페이지 번호 리스트 (검색어가 없으면 빈 리스트)
     */
    public static List<Integer> findPagesWithTerm(String ocrText, String searchTerm) {
        List<Integer> pages = new ArrayList<>();

        if (ocrText == null || ocrText.isEmpty() || searchTerm == null || searchTerm.isEmpty()) {
            return pages;
        }

        String lowerOcrText = ocrText.toLowerCase();
        String lowerSearchTerm = searchTerm.toLowerCase();

        // 페이지별로 분리
        String[] sections = ocrText.split("--- Page \\d+ ---");
        Matcher matcher = PAGE_MARKER_PATTERN.matcher(ocrText);

        int sectionIndex = 1; // 첫 번째 section은 페이지 마커 이전 (보통 비어있음)
        while (matcher.find() && sectionIndex < sections.length) {
            int pageNum = Integer.parseInt(matcher.group(1));
            String pageContent = sections[sectionIndex];

            if (pageContent.toLowerCase().contains(lowerSearchTerm)) {
                pages.add(pageNum);
            }
            sectionIndex++;
        }

        return pages;
    }

    /**
     * 검색어가 포함된 페이지 번호를 문자열로 반환
     *
     * @param ocrText OCR 추출 텍스트
     * @param searchTerm 검색어
     * @return "2, 5, 7페이지" 형식의 문자열 (없으면 빈 문자열)
     */
    public static String formatPagesWithTerm(String ocrText, String searchTerm) {
        List<Integer> pages = findPagesWithTerm(ocrText, searchTerm);

        if (pages.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(pages.get(i));
        }
        result.append("페이지");

        return result.toString();
    }

    /**
     * OCR 텍스트에서 총 페이지 수 추출
     *
     * @param ocrText OCR 추출 텍스트
     * @return 총 페이지 수 (마커가 없으면 0)
     */
    public static int getTotalPages(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) {
            return 0;
        }

        Matcher matcher = PAGE_MARKER_PATTERN.matcher(ocrText);
        int maxPage = 0;

        while (matcher.find()) {
            int pageNum = Integer.parseInt(matcher.group(1));
            if (pageNum > maxPage) {
                maxPage = pageNum;
            }
        }

        return maxPage;
    }

    /**
     * 특정 페이지의 텍스트 추출
     *
     * @param ocrText OCR 추출 텍스트
     * @param pageNumber 페이지 번호 (1부터 시작)
     * @return 해당 페이지의 텍스트 (없으면 null)
     */
    public static String extractPageText(String ocrText, int pageNumber) {
        if (ocrText == null || ocrText.isEmpty() || pageNumber < 1) {
            return null;
        }

        Pattern pagePattern = Pattern.compile(
            "--- Page " + pageNumber + " ---\\n(.*?)(?=--- Page \\d+ ---|$)",
            Pattern.DOTALL
        );

        Matcher matcher = pagePattern.matcher(ocrText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
}
