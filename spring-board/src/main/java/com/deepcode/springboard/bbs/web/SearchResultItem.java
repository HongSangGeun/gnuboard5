package com.deepcode.springboard.bbs.web;

import com.deepcode.springboard.bbs.domain.Write;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 검색 결과 아이템 (페이지 정보 포함)
 */
@Getter
@Setter
public class SearchResultItem {
    private Write post;
    private List<Integer> matchedPages;  // 검색어가 발견된 페이지 번호들
    private String matchedPagesText;     // "2, 5, 7페이지" 형식

    public SearchResultItem(Write post) {
        this.post = post;
    }

    public SearchResultItem(Write post, List<Integer> matchedPages, String matchedPagesText) {
        this.post = post;
        this.matchedPages = matchedPages;
        this.matchedPagesText = matchedPagesText;
    }

    public boolean hasPageInfo() {
        return matchedPages != null && !matchedPages.isEmpty();
    }
}
