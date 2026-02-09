package com.deepcode.springboard.bbs.web;

import com.deepcode.springboard.bbs.domain.Board;
import com.deepcode.springboard.bbs.domain.BoardFile;
import com.deepcode.springboard.bbs.domain.Write;
import com.deepcode.springboard.bbs.mapper.BoardFileMapper;
import com.deepcode.springboard.bbs.service.BoardPermissionDeniedException;
import com.deepcode.springboard.bbs.service.BoardService;
import com.deepcode.springboard.bbs.service.WriteTableValidator;
import com.deepcode.springboard.bbs.util.OcrTextUtil;
import com.deepcode.springboard.board.BoardGroup;
import com.deepcode.springboard.board.BoardGroupMapper;
import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.member.LoginUser;
import com.deepcode.springboard.member.SearchCriteria;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SearchController {
    private static final int DEFAULT_ROWS = 10;
    private static final int MAX_ROWS = 100;
    private static final Set<String> ALLOWED_SFL = Set.of(
            "wr_subject||wr_content",
            "wr_subject||wr_content||bf_ocr_text",
            "wr_subject",
            "wr_content",
            "bf_ocr_text",
            "mb_id",
            "wr_name");

    private final BoardService boardService;
    private final BoardGroupMapper boardGroupMapper;
    private final BoardFileMapper boardFileMapper;

    public SearchController(BoardService boardService, BoardGroupMapper boardGroupMapper, BoardFileMapper boardFileMapper) {
        this.boardService = boardService;
        this.boardGroupMapper = boardGroupMapper;
        this.boardFileMapper = boardFileMapper;
    }

    @GetMapping({"/bbs/search", "/bbs/search/list"})
    public String search(@RequestParam(name = "stx", required = false) String stx,
            @RequestParam(name = "sfl", required = false, defaultValue = "wr_subject||wr_content") String sfl,
            @RequestParam(name = "sop", required = false, defaultValue = "and") String sop,
            @RequestParam(name = "srows", required = false, defaultValue = "10") int srows,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "gr_id", required = false) String grId,
            @RequestParam(name = "onetable", required = false) String onetable,
            Model model,
            HttpSession session) {
        String safeStx = normalizeSearchText(stx);
        String safeSfl = normalizeSearchField(sfl);
        String safeSop = "or".equalsIgnoreCase(sop) ? "or" : "and";
        int safeRows = Math.min(Math.max(srows, 1), MAX_ROWS);
        int safePage = Math.max(page, 1);
        String safeGrId = normalizeGroupId(grId);
        String safeOneTable = normalizeBoardTable(onetable);

        LoginUser loginUser = loginUser(session);

        List<Board> targetBoards = boardService.listBoards().stream()
                .filter(board -> isSearchAccessible(board, loginUser))
                .filter(board -> safeGrId == null || safeGrId.equals(board.getGrId()))
                .filter(board -> safeOneTable == null || safeOneTable.equals(board.getBoTable()))
                .toList();

        List<BoardTab> boardTabs = new ArrayList<>();
        List<SearchBoardResult> boardResults = new ArrayList<>();
        int totalCount = 0;
        int boardCount = 0;
        int totalPage = 0;
        int currentPage = safePage;

        if (hasText(safeStx)) {
            List<BoardCounter> counters = new ArrayList<>();
            int cumulative = 0;
            for (Board board : targetBoards) {
                int count = boardService.countPosts(board.getBoTable(), safeSfl, safeStx, safeSop, null, null, null);
                if (count <= 0) {
                    continue;
                }
                cumulative += count;
                counters.add(new BoardCounter(board, count, cumulative));
                boardTabs.add(new BoardTab(board.getBoTable(), board.getBoSubject(), count));
                totalCount += count;
            }

            boardCount = counters.size();
            totalPage = totalCount > 0 ? (int) Math.ceil(totalCount / (double) safeRows) : 0;
            if (totalPage > 0 && currentPage > totalPage) {
                currentPage = totalPage;
            }

            if (!counters.isEmpty()) {
                int fromRecord = (currentPage - 1) * safeRows;
                int tableIndex = 0;
                int prevCumulative = 0;
                while (tableIndex < counters.size() && fromRecord >= counters.get(tableIndex).cumulativeCount()) {
                    prevCumulative = counters.get(tableIndex).cumulativeCount();
                    tableIndex++;
                }

                int offsetInBoard = Math.max(fromRecord - prevCumulative, 0);
                int fetched = 0;
                for (int idx = tableIndex; idx < counters.size() && fetched < safeRows; idx++) {
                    BoardCounter counter = counters.get(idx);
                    int need = safeRows - fetched;
                    List<Write> posts = boardService.listPostsByOffset(
                            counter.board().getBoTable(),
                            offsetInBoard,
                            need,
                            safeSfl,
                            safeStx,
                            safeSop,
                            null,
                            null,
                            null,
                            "wr_id",
                            "desc");

                    if (!posts.isEmpty()) {
                        List<SearchItem> items = new ArrayList<>();
                        boolean isOcrSearch = safeSfl != null && safeSfl.contains("bf_ocr_text");

                        for (Write post : posts) {
                            long targetWrId = post.getWrParent() > 0 ? post.getWrParent() : post.getWrId();
                            String href = "/bbs/" + counter.board().getBoTable() + "/view/" + targetWrId;

                            // OCR 검색 시 페이지 정보 추출
                            String pageInfo = "";
                            if (isOcrSearch && hasText(safeStx)) {
                                pageInfo = extractPageInfo(counter.board().getBoTable(), post.getWrId(), safeStx);
                            }

                            items.add(new SearchItem(
                                    targetWrId,
                                    post.getWrIsComment() == 1,
                                    defaultString(post.getWrSubject()),
                                    highlightSubject(post.getWrSubject(), safeStx),
                                    buildSnippet(post.getWrContent()),
                                    defaultString(post.getWrName()),
                                    defaultString(post.getWrDatetime()),
                                    href,
                                    pageInfo));
                        }
                        boardResults.add(new SearchBoardResult(
                                counter.board().getBoTable(),
                                defaultString(counter.board().getBoSubject()),
                                items));
                        fetched += posts.size();
                    }
                    offsetInBoard = 0;
                }
            }
        }

        List<Integer> pageNumbers = buildPageNumbers(currentPage, totalPage, boardService.getWritePages());

        SearchCriteria groupCriteria = new SearchCriteria();
        groupCriteria.setPage(1);
        groupCriteria.setLimit(1000);
        groupCriteria.setSst("gr_id");
        groupCriteria.setSod("asc");
        List<BoardGroup> groups = boardGroupMapper.findAll(groupCriteria);

        model.addAttribute("pageTitle", "전체검색 결과");
        model.addAttribute("textStx", safeStx);
        model.addAttribute("sfl", safeSfl);
        model.addAttribute("sop", safeSop);
        model.addAttribute("srows", safeRows);
        model.addAttribute("grId", safeGrId == null ? "" : safeGrId);
        model.addAttribute("onetable", safeOneTable == null ? "" : safeOneTable);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("boardCount", boardCount);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("hasPrevPage", currentPage > 1);
        model.addAttribute("hasNextPage", totalPage > 0 && currentPage < totalPage);
        model.addAttribute("prevPage", Math.max(currentPage - 1, 1));
        model.addAttribute("nextPage", totalPage > 0 ? Math.min(currentPage + 1, totalPage) : 1);
        model.addAttribute("boardTabs", boardTabs);
        model.addAttribute("boardResults", boardResults);
        model.addAttribute("groupOptions", groups);

        return "bbs/search/list";
    }

    private List<Integer> buildPageNumbers(int currentPage, int totalPage, int blockSize) {
        List<Integer> numbers = new ArrayList<>();
        if (totalPage <= 0) {
            return numbers;
        }
        int safeBlockSize = Math.max(blockSize, 1);
        int blockIndex = (currentPage - 1) / safeBlockSize;
        int start = blockIndex * safeBlockSize + 1;
        int end = Math.min(start + safeBlockSize - 1, totalPage);
        for (int i = start; i <= end; i++) {
            numbers.add(i);
        }
        return numbers;
    }

    private String normalizeSearchText(String value) {
        if (!hasText(value)) {
            return "";
        }
        String stripped = value.replaceAll("<[^>]*>", " ");
        stripped = stripped.replaceAll("[\\p{Cntrl}]+", " ");
        stripped = stripped.trim().replaceAll("\\s+", " ");
        if (!hasText(stripped)) {
            return "";
        }
        String[] terms = stripped.split(" ");
        if (terms.length > 2) {
            return terms[0] + " " + terms[1];
        }
        return stripped;
    }

    private String normalizeSearchField(String value) {
        if (!hasText(value)) {
            return "wr_subject||wr_content";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("mb_id")) {
            normalized = "mb_id";
        } else if (normalized.startsWith("wr_name")) {
            normalized = "wr_name";
        }
        if (ALLOWED_SFL.contains(normalized)) {
            return normalized;
        }
        return "wr_subject||wr_content";
    }

    private String normalizeGroupId(String value) {
        if (!hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.matches("^[A-Za-z0-9_]{1,10}$")) {
            return normalized;
        }
        return null;
    }

    private String normalizeBoardTable(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return WriteTableValidator.validateBoTable(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String buildSnippet(String html) {
        if (!hasText(html)) {
            return "";
        }
        String text = html.replaceAll("(?is)<script[^>]*>.*?</script>", " ");
        text = text.replaceAll("<[^>]+>", " ");
        text = text.replace("&nbsp;", " ");
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() > 300) {
            return text.substring(0, 300) + "…";
        }
        return text;
    }

    private LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    private boolean isSearchAccessible(Board board, LoginUser loginUser) {
        try {
            boardService.assertListable(board, loginUser);
            return true;
        } catch (BoardPermissionDeniedException ex) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    /**
     * HTML 특수문자를 이스케이프한 후 검색어를 하이라이트 span으로 감싸 반환.
     * 결과는 th:utext로 출력해야 하므로, 검색어 외의 부분은 반드시 이스케이프된 상태.
     */
    private String highlightSubject(String subject, String searchTerm) {
        if (!hasText(subject) || !hasText(searchTerm)) {
            return escapeHtml(subject);
        }
        String escaped = escapeHtml(subject);
        String escapedTerm = escapeHtml(searchTerm);
        if (!hasText(escapedTerm)) {
            return escaped;
        }
        return escaped.replace(escapedTerm,
                "<span class=\"sch_highlight\">" + escapedTerm + "</span>");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * OCR 검색 시 페이지 정보 추출
     */
    private String extractPageInfo(String boTable, long wrId, String searchTerm) {
        try {
            List<BoardFile> files = boardFileMapper.findByPost(boTable, wrId);
            List<Integer> allMatchedPages = new ArrayList<>();

            for (BoardFile file : files) {
                if (file.getBfOcrText() != null && !file.getBfOcrText().isEmpty()) {
                    List<Integer> pages = OcrTextUtil.findPagesWithTerm(file.getBfOcrText(), searchTerm);
                    allMatchedPages.addAll(pages);
                }
            }

            if (!allMatchedPages.isEmpty() && !files.isEmpty()) {
                return OcrTextUtil.formatPagesWithTerm(files.get(0).getBfOcrText(), searchTerm);
            }
        } catch (Exception e) {
            log.warn("Error extracting page info for {}/{}: {}", boTable, wrId, e.getMessage());
        }
        return "";
    }

    private record BoardCounter(Board board, int count, int cumulativeCount) {
    }

    public record BoardTab(String boTable, String boSubject, int count) {
    }

    public record SearchBoardResult(String boTable, String boSubject, List<SearchItem> items) {
    }

    public record SearchItem(long wrId, boolean comment, String subject, String highlightedSubject, String content, String writer, String wrDatetime,
            String href, String pageInfo) {

        public boolean hasPageInfo() {
            return pageInfo != null && !pageInfo.isEmpty();
        }
    }
}
