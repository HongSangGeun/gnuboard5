package com.deepcode.springboard.home;

import com.deepcode.springboard.bbs.domain.Board;
import com.deepcode.springboard.bbs.domain.Write;
import com.deepcode.springboard.bbs.mapper.BoardMapper;
import com.deepcode.springboard.bbs.service.BoardService;
import com.deepcode.springboard.common.PagedResult;
import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.config.G5ConfigService;
import com.deepcode.springboard.health.MonitoringService;
import com.deepcode.springboard.health.MonitoringServiceManager;
import com.deepcode.springboard.member.LoginUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class HomeController {
    private final BoardMapper boardMapper;
    private final BoardService boardService;
    private final G5ConfigService configService;
    private final MonitoringServiceManager monitoringServiceManager;

    public HomeController(BoardMapper boardMapper, BoardService boardService, G5ConfigService configService,
                         MonitoringServiceManager monitoringServiceManager) {
        this.boardMapper = boardMapper;
        this.boardService = boardService;
        this.configService = configService;
        this.monitoringServiceManager = monitoringServiceManager;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        if (configService.getAdminId().isBlank()) {
            return "redirect:/install";
        }
        LoginUser user = loginUser(session);
        if (user == null) {
            String url = URLEncoder.encode("/", StandardCharsets.UTF_8);
            return "redirect:/login?url=" + url;
        }

        List<Board> boards = boardMapper.findAll();
        List<HomeCard> cards = new ArrayList<>();
        List<MyPostItem> myPosts = new ArrayList<>();
        Board scheduleBoard = null;

        for (Board board : boards) {
            if ("schedule".equals(board.getBoTable())) {
                scheduleBoard = board;
            }
            if ("mobile".equalsIgnoreCase(board.getBoDevice())) {
                continue;
            }
            try {
                boardService.assertListable(board, user);
            } catch (Exception e) {
                continue;
            }

            List<Write> mine = boardService.listPostsByMemberWithComments(board.getBoTable(), user.getId(), 12);
            applyLatestFlags(board, mine);
            Set<String> addedCommentByParent = new HashSet<>();
            for (Write post : mine) {
                if (post.getWrIsComment() == 1) {
                    if (post.getWrParent() <= 0) {
                        continue;
                    }
                    String commentGroupKey = board.getBoTable() + ":" + post.getWrParent();
                    if (!addedCommentByParent.add(commentGroupKey)) {
                        continue;
                    }
                }
                myPosts.add(new MyPostItem(board, post));
            }

            if ("notice".equals(board.getBoTable()) || "gallery".equals(board.getBoTable()) || "schedule".equals(board.getBoTable())) {
                continue;
            }
            PagedResult<Write> latest = boardService.listPosts(
                    board.getBoTable(),
                    1,
                    6,
                    null,
                    null,
                    "and",
                    null,
                    null,
                    null,
                    "wr_datetime",
                    "desc");
            applyLatestFlags(board, latest.getItems());
            cards.add(new HomeCard(board, latest.getItems()));
        }

        myPosts.sort(Comparator.comparing(MyPostItem::postedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        if (myPosts.size() > 8) {
            myPosts = new ArrayList<>(myPosts.subList(0, 8));
        }

        // 모니터링 서비스 목록
        List<MonitoringService> monitoringServices = monitoringServiceManager.getEnabledServices();

        model.addAttribute("cards", cards);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("scheduleBoard", scheduleBoard);
        model.addAttribute("scheduleCategories", parseCategories(scheduleBoard));
        model.addAttribute("monitoringServices", monitoringServices);
        model.addAttribute("hasMonitoring", !monitoringServices.isEmpty());
        model.addAttribute("pageTitle", "대시보드");
        return "index";
    }

    private List<String> parseCategories(Board board) {
        if (board == null || board.getBoCategoryList() == null || board.getBoCategoryList().isBlank()) {
            return Arrays.asList("유지보수", "하드웨어", "데이터허브", "응급의료", "집중관제", "기타");
        }
        String[] parts = board.getBoCategoryList().split("\\|");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                list.add(p.trim());
            }
        }
        return list.isEmpty() ? Arrays.asList("유지보수", "하드웨어", "데이터허브", "응급의료", "집중관제", "기타") : list;
    }

    private void applyLatestFlags(Board board, List<Write> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        int newHours = board != null && board.getBoNew() > 0 ? board.getBoNew() : 24;
        int hotHit = board != null && board.getBoHot() > 0 ? board.getBoHot() : 0;
        LocalDateTime threshold = LocalDateTime.now().minusHours(newHours);

        for (Write post : posts) {
            LocalDateTime postedAt = parsePostDatetime(post.getWrDatetime());
            if (postedAt != null && !postedAt.isBefore(threshold)) {
                post.setNewPost(true);
            }
            if (hotHit > 0 && post.getWrHit() >= hotHit) {
                post.setHotPost(true);
            }
            post.setFileAttached(post.getWrFile() > 0);
            post.setLinkAttached(hasText(post.getWrLink1()) || hasText(post.getWrLink2()));
        }
    }

    private LocalDateTime parsePostDatetime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace('T', ' ');
        normalized = normalized.replaceAll("([+-]\\d{2}:\\d{2}|Z)$", "").trim();
        if (normalized.matches("\\d{14}")) {
            normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8)
                    + " " + normalized.substring(8, 10) + ":" + normalized.substring(10, 12) + ":" + normalized.substring(12, 14);
        } else if (normalized.matches("\\d{8}")) {
            normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8) + " 00:00:00";
        } else if (normalized.matches("\\d{4}-\\d{2}-\\d{2}$")) {
            normalized = normalized + " 00:00:00";
        } else if (normalized.length() > 19) {
            normalized = normalized.substring(0, 19);
        }
        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser) {
            return (LoginUser) value;
        }
        return null;
    }

    public static class HomeCard {
        private final Board board;
        private final List<Write> posts;

        public HomeCard(Board board, List<Write> posts) {
            this.board = board;
            this.posts = posts;
        }

        public Board getBoard() {
            return board;
        }

        public List<Write> getPosts() {
            return posts;
        }
    }

    public static class MyPostItem {
        private final Board board;
        private final Write post;
        private final LocalDateTime postedAt;

        public MyPostItem(Board board, Write post) {
            this.board = board;
            this.post = post;
            this.postedAt = parseDatetime(post != null ? post.getWrDatetime() : null);
        }

        public Board getBoard() {
            return board;
        }

        public Write getPost() {
            return post;
        }

        public LocalDateTime postedAt() {
            return postedAt;
        }

        public boolean isComment() {
            return post != null && post.getWrIsComment() == 1;
        }

        public long getViewTargetId() {
            if (post == null) {
                return 0L;
            }
            if (isComment() && post.getWrParent() > 0) {
                return post.getWrParent();
            }
            return post.getWrId();
        }

        public String getDisplayTitle() {
            if (post == null) {
                return "";
            }
            if (!isComment()) {
                return defaultText(post.getWrSubject(), "(제목 없음)");
            }
            String content = sanitizeText(post.getWrContent());
            if (content.isBlank()) {
                return "(댓글 내용 없음)";
            }
            if (content.length() > 28) {
                return content.substring(0, 28) + "...";
            }
            return content;
        }

        private static String sanitizeText(String value) {
            if (value == null) {
                return "";
            }
            return value.replaceAll("<[^>]*>", " ")
                    .replace("&nbsp;", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        }

        private static String defaultText(String value, String fallback) {
            if (value == null || value.isBlank()) {
                return fallback;
            }
            return value;
        }

        private static LocalDateTime parseDatetime(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            String normalized = value.trim().replace('T', ' ');
            normalized = normalized.replaceAll("([+-]\\d{2}:\\d{2}|Z)$", "").trim();
            if (normalized.matches("\\d{14}")) {
                normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8)
                        + " " + normalized.substring(8, 10) + ":" + normalized.substring(10, 12) + ":" + normalized.substring(12, 14);
            } else if (normalized.matches("\\d{8}")) {
                normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8) + " 00:00:00";
            } else if (normalized.matches("\\d{4}-\\d{2}-\\d{2}$")) {
                normalized = normalized + " 00:00:00";
            } else if (normalized.length() > 19) {
                normalized = normalized.substring(0, 19);
            }
            try {
                return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
