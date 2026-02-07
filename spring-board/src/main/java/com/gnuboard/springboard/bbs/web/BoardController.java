package com.gnuboard.springboard.bbs.web;

import com.gnuboard.springboard.bbs.domain.Board;
import com.gnuboard.springboard.bbs.domain.BoardFile;
import com.gnuboard.springboard.bbs.domain.Write;
import com.gnuboard.springboard.bbs.domain.ViewFile;
import com.gnuboard.springboard.bbs.service.BoardPermissionDeniedException;
import com.gnuboard.springboard.bbs.service.BoardService;
import com.gnuboard.springboard.bbs.service.FileStorageService;
import com.gnuboard.springboard.common.PagedResult;
import com.gnuboard.springboard.common.SessionConst;
import com.gnuboard.springboard.member.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class BoardController {
    private static final Pattern BBS_TABLE_PATTERN = Pattern.compile("/bbs/([^/]+)");
    private final BoardService boardService;
    private final FileStorageService fileStorageService;
    private final SkinViewResolver skinViewResolver;

    public BoardController(BoardService boardService, FileStorageService fileStorageService,
            SkinViewResolver skinViewResolver) {
        this.boardService = boardService;
        this.fileStorageService = fileStorageService;
        this.skinViewResolver = skinViewResolver;
    }

    @GetMapping("/bbs")
    public String index(Model model, HttpSession session) {
        LoginUser loginUser = loginUser(session);
        List<Board> boards = boardService.listBoards().stream()
                .filter(board -> isListable(board, loginUser))
                .toList();
        model.addAttribute("boards", boards);
        return "bbs/index";
    }

    @GetMapping("/bbs/{boTable}")
    public String boardRoot(@PathVariable String boTable) {
        if ("search".equalsIgnoreCase(boTable)) {
            return "forward:/bbs/search";
        }
        return "redirect:/bbs/" + boTable + "/list";
    }

    @GetMapping("/bbs/{boTable}/list")
    public String list(@PathVariable String boTable,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(required = false) String sfl,
            @RequestParam(required = false) String stx,
            @RequestParam(defaultValue = "and") String sop,
            @RequestParam(required = false) String sca,
            @RequestParam(required = false) String sdate,
            @RequestParam(required = false) String edate,
            @RequestParam(required = false) String sst,
            @RequestParam(defaultValue = "") String sod,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
        if ("search".equalsIgnoreCase(boTable)) {
            return "forward:/bbs/search";
        }
        Board board = boardService.getBoardOrThrow(boTable);
        boardService.assertListable(board, loginUser(session));
        int pageSize = size > 0 ? size : (board.getBoPageRows() > 0 ? board.getBoPageRows() : 20);
        PagedResult<Write> result = boardService.listPosts(boTable, page, pageSize, sfl, stx, sop, sca, sdate, edate, sst, sod);
        applyListFlags(board, result.getItems());
        applyPaginationModel(model, result, boardService.getWritePages());
        applyGalleryExtras(model, board, boTable, result.getItems(), request);

        model.addAttribute("board", board);
        model.addAttribute("result", result);
        model.addAttribute("boTable", boTable);
        model.addAttribute("sfl", sfl);
        model.addAttribute("stx", stx);
        model.addAttribute("sop", sop);
        model.addAttribute("sca", sca);
        model.addAttribute("sdate", sdate);
        model.addAttribute("edate", edate);
        model.addAttribute("sst", sst);
        model.addAttribute("sod", sod);

        return skinViewResolver.resolve(board, "list");
    }

    @GetMapping("/bbs/{boTable}/view/{wrId}")
    public String view(@PathVariable String boTable,
            @PathVariable long wrId,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        Board board = boardService.getBoardOrThrow(boTable);
        boardService.assertReadable(board, loginUser(session));
        Write post = boardService.getPost(boTable, wrId);
        if (post == null) {
            redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
            return "redirect:/bbs/" + boTable + "/list";
        }
        List<Write> comments = boardService.listComments(boTable, wrId);
        List<BoardFile> files = boardService.listFiles(boTable, wrId);
        List<ViewFile> viewFiles = buildViewFiles(boTable, wrId, files);

        model.addAttribute("board", board);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("files", files);
        model.addAttribute("viewFiles", viewFiles);
        model.addAttribute("boTable", boTable);
        model.addAttribute("commentForm", new CommentForm());

        return skinViewResolver.resolve(board, "view");
    }

    @GetMapping("/bbs/{boTable}/write")
    public String writeForm(@PathVariable String boTable,
            @RequestParam(name = "wr_1", required = false) String wr1,
            @RequestParam(name = "wr_2", required = false) String wr2,
            @RequestParam(name = "allday", required = false) String allday,
            Model model,
            HttpSession session) {
        Board board = boardService.getBoardOrThrow(boTable);
        boardService.assertWritable(board, loginUser(session));
        model.addAttribute("board", board);
        model.addAttribute("boTable", boTable);
        WriteForm form = new WriteForm();
        form.setWr1(toDateTimeLocal(wr1));
        form.setWr2(toDateTimeLocal(wr2));
        form.setWr5(allday);
        model.addAttribute("writeForm", form);
        return skinViewResolver.resolve(board, "write");
    }

    @PostMapping("/bbs/{boTable}/write")
    public String write(@PathVariable String boTable,
            @ModelAttribute WriteForm writeForm,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpSession session) {
        try {
            Board board = boardService.getBoardOrThrow(boTable);
            boardService.assertWritable(board, loginUser(session));
            long wrId = boardService.createPost(
                    boTable,
                    writeForm.getSubject(),
                    writeForm.getContent(),
                    writeForm.getName(),
                    writeForm.getPassword(),
                    writeForm.getEmail(),
                    writeForm.getHomepage(),
                    request.getRemoteAddr(),
                    writeForm.getCaName(),
                    writeForm.getWr1(),
                    writeForm.getWr2(),
                    writeForm.getWr4(),
                    writeForm.getWr5(),
                    loginUser(session),
                    files != null ? Arrays.asList(files) : List.of());

            if (wrId <= 0) {
                redirectAttributes.addFlashAttribute("error", "게시글 저장에 실패했습니다.");
                return "redirect:/bbs/" + boTable + "/list";
            }

            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/bbs/" + boTable + "/write";
        }
    }

    @GetMapping("/bbs/{boTable}/edit/{wrId}")
    public String editForm(@PathVariable String boTable,
            @PathVariable long wrId,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        Board board = boardService.getBoardOrThrow(boTable);
        boardService.assertWritable(board, loginUser(session));
        Write post = boardService.getPost(boTable, wrId);
        if (post == null) {
            redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
            return "redirect:/bbs/" + boTable + "/list";
        }
        if (post.getMbId() != null && !post.getMbId().isBlank()) {
            LoginUser user = loginUser(session);
            if (user == null || !post.getMbId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "권한이 없습니다.");
                return "redirect:/bbs/" + boTable + "/view/" + wrId;
            }
        }
        WriteForm form = new WriteForm();
        form.setSubject(post.getWrSubject());
        form.setContent(post.getWrContent());
        form.setName(post.getWrName());
        form.setEmail(post.getWrEmail());
        form.setHomepage(post.getWrHomepage());
        form.setCaName(post.getCaName());
        form.setWr1(toDateTimeLocal(post.getWr1()));
        form.setWr2(toDateTimeLocal(post.getWr2()));
        form.setWr4(post.getWr4());
        form.setWr5(post.getWr5());

        model.addAttribute("board", board);
        model.addAttribute("post", post);
        model.addAttribute("boTable", boTable);
        model.addAttribute("writeForm", form);
        model.addAttribute("files", boardService.listFiles(boTable, wrId));
        return skinViewResolver.resolve(board, "edit");
    }

    @PostMapping("/bbs/{boTable}/edit/{wrId}")
    public String edit(@PathVariable String boTable,
            @PathVariable long wrId,
            @ModelAttribute WriteForm writeForm,
            @RequestParam(name = "deleteFiles", required = false) Integer[] deleteFiles,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpSession session) {
        try {
            Board board = boardService.getBoardOrThrow(boTable);
            boardService.assertWritable(board, loginUser(session));
            boardService.updatePost(
                    boTable,
                    wrId,
                    writeForm.getSubject(),
                    writeForm.getContent(),
                    writeForm.getName(),
                    writeForm.getPassword(),
                    writeForm.getEmail(),
                    writeForm.getHomepage(),
                    request.getRemoteAddr(),
                    writeForm.getCaName(),
                    writeForm.getWr1(),
                    writeForm.getWr2(),
                    writeForm.getWr4(),
                    writeForm.getWr5(),
                    loginUser(session),
                    files != null ? Arrays.asList(files) : List.of(),
                    deleteFiles != null ? Arrays.asList(deleteFiles) : List.of());
            redirectAttributes.addFlashAttribute("message", "수정되었습니다.");
            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/bbs/" + boTable + "/edit/" + wrId;
        }
    }

    @PostMapping("/bbs/{boTable}/delete/{wrId}")
    public String delete(@PathVariable String boTable,
            @PathVariable long wrId,
            @ModelAttribute PasswordForm form,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {
            boardService.deletePost(boTable, wrId, form.getPassword(), loginUser(session));
            redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
            return "redirect:/bbs/" + boTable + "/list";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        }
    }

    @PostMapping("/bbs/{boTable}/comment/{wrId}")
    public String comment(@PathVariable String boTable,
            @PathVariable long wrId,
            @ModelAttribute CommentForm form,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpSession session) {
        try {
            Board board = boardService.getBoardOrThrow(boTable);
            boardService.assertCommentable(board, loginUser(session));
            boardService.createComment(
                    boTable,
                    wrId,
                    form.getContent(),
                    form.getName(),
                    form.getPassword(),
                    request.getRemoteAddr(),
                    loginUser(session),
                    form.getReplyTo());
            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        }
    }

    @PostMapping("/bbs/{boTable}/comment/{wrId}/edit/{commentId}")
    public String commentEdit(@PathVariable String boTable,
            @PathVariable long wrId,
            @PathVariable long commentId,
            @RequestParam("content") String content,
            @RequestParam(name = "password", required = false) String password,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpSession session) {
        try {
            boardService.updateComment(
                    boTable,
                    wrId,
                    commentId,
                    content,
                    password,
                    request.getRemoteAddr(),
                    loginUser(session));
            redirectAttributes.addFlashAttribute("message", "댓글이 수정되었습니다.");
            return "redirect:/bbs/" + boTable + "/view/" + wrId + "#c_" + commentId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        }
    }

    @PostMapping("/bbs/{boTable}/comment/{wrId}/delete/{commentId}")
    public String commentDelete(@PathVariable String boTable,
            @PathVariable long wrId,
            @PathVariable long commentId,
            @ModelAttribute PasswordForm form,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {
            boardService.deleteComment(
                    boTable,
                    wrId,
                    commentId,
                    form.getPassword(),
                    loginUser(session));
            redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/bbs/" + boTable + "/view/" + wrId;
        }
    }

    @GetMapping("/bbs/{boTable}/download/{wrId}/{bfNo}")
    public ResponseEntity<Resource> download(@PathVariable String boTable,
                                             @PathVariable long wrId,
                                             @PathVariable int bfNo,
                                             HttpSession session) {
        Board board = boardService.getBoardOrThrow(boTable);
        boardService.assertReadable(board, loginUser(session));
        BoardFile file = boardService.getFile(boTable, wrId, bfNo);
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }
        Path path = fileStorageService.resolveFile(boTable, file.getBfFile());
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }
        boardService.incrementFileDownload(boTable, wrId, bfNo);
        String source = (file.getBfSource() == null || file.getBfSource().isBlank()) ? file.getBfFile()
                : file.getBfSource();
        String encoded = URLEncoder.encode(source, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
                .contentLength(file.getBfFilesize())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @GetMapping("/bbs/{boTable}/file/{wrId}/{bfNo}")
    public ResponseEntity<Resource> viewFile(@PathVariable String boTable,
                                             @PathVariable long wrId,
                                             @PathVariable int bfNo,
                                             HttpSession session) {
        Board board = boardService.getBoardOrThrow(boTable);
        boardService.assertReadable(board, loginUser(session));
        BoardFile file = boardService.getFile(boTable, wrId, bfNo);
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }
        ViewFile viewFile = toViewFile(boTable, wrId, file);
        if (viewFile.viewType() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "미리보기를 지원하지 않습니다.");
        }
        Path path = fileStorageService.resolveFile(boTable, file.getBfFile());
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }

        MediaType mediaType = resolveInlineMediaType(path, viewFile.viewType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(mediaType)
                .body(resource);
    }

    private List<ViewFile> buildViewFiles(String boTable, long wrId, List<BoardFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        List<ViewFile> result = new ArrayList<>();
        for (BoardFile file : files) {
            result.add(toViewFile(boTable, wrId, file));
        }
        return result;
    }

    private ViewFile toViewFile(String boTable, long wrId, BoardFile file) {
        String name = file.getBfSource();
        if (name == null || name.isBlank()) {
            name = file.getBfFile();
        }
        String ext = "";
        int dot = name != null ? name.lastIndexOf('.') : -1;
        if (dot >= 0 && dot < name.length() - 1) {
            ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        String viewType = switch (ext) {
            case "jpg", "jpeg", "png", "gif", "webp" -> "image";
            case "pdf" -> "pdf";
            case "txt" -> "text";
            default -> null;
        };
        String viewUrl = viewType != null ? "/bbs/" + boTable + "/file/" + wrId + "/" + file.getBfNo() : null;
        return new ViewFile(file, viewType, viewUrl);
    }

    private MediaType resolveInlineMediaType(Path path, String viewType) {
        try {
            String detected = Files.probeContentType(path);
            if (detected != null && !detected.isBlank()) {
                MediaType mediaType = MediaType.parseMediaType(detected);
                if (isInlineMediaType(mediaType, viewType)) {
                    return mediaType;
                }
            }
        } catch (IOException ignored) {
        }
        if (Objects.equals(viewType, "image")) {
            return MediaType.IMAGE_JPEG;
        }
        if (Objects.equals(viewType, "pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        return MediaType.TEXT_PLAIN;
    }

    private boolean isInlineMediaType(MediaType mediaType, String viewType) {
        if (viewType == null) {
            return false;
        }
        if ("image".equals(viewType)) {
            return mediaType.getType().equalsIgnoreCase("image");
        }
        if ("pdf".equals(viewType)) {
            return mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_PDF);
        }
        if ("text".equals(viewType)) {
            return mediaType.getType().equalsIgnoreCase("text");
        }
        return false;
    }

    @GetMapping("/bbs/{boTable}/events")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<Map<String, Object>> events(@PathVariable String boTable, HttpServletRequest request, HttpSession session) {
        Board board = boardService.getBoardOrThrow(boTable);
        boardService.assertListable(board, loginUser(session));
        PagedResult<Write> result = boardService.listPosts(boTable, 1, 300, null, null, "and", null, null, null, "wr_datetime", "desc");
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Write post : result.getItems()) {
            String start = toIsoDateTime(preferScheduleStart(post));
            if (start == null) {
                start = toIsoDateTime(post.getWrDatetime());
            }
            String end = toIsoDateTime(post.getWr2());
            if (start == null) {
                continue;
            }
            Map<String, Object> ev = new HashMap<>();
            ev.put("title", post.getWrSubject());
            ev.put("start", start);
            boolean allDay = "1".equals(post.getWr5());
            if (allDay) {
                ev.put("allDay", true);
            }
            String computedEnd = computeEventEnd(start, end, allDay);
            if (computedEnd != null && !computedEnd.equals(start)) {
                ev.put("end", computedEnd);
            }
            ev.put("url", contextPath + "/bbs/" + boTable + "/view/" + post.getWrId());
            if (post.getCaName() != null && !post.getCaName().isBlank()) {
                ev.put("category", post.getCaName());
            }
            ev.put("commentCount", post.getWrComment());
            if (post.getWr4() != null && !post.getWr4().isBlank()) {
                ev.put("color", post.getWr4());
            }
            items.add(ev);
        }
        return items;
    }

    private void applyPaginationModel(Model model, PagedResult<Write> result, int writePages) {
        int totalPages = result.getTotalPages();
        int currentPage = Math.max(result.getPage(), 1);
        int blockSize = writePages > 0 ? writePages : 10;

        int pageStart = 0;
        int pageEnd = 0;
        int prevBlockPage = 1;
        int nextBlockPage = 1;
        boolean hasPrevBlock = false;
        boolean hasNextBlock = false;
        List<Integer> pageNumbers = new ArrayList<>();

        if (totalPages > 0) {
            int blockIndex = (currentPage - 1) / blockSize;
            pageStart = blockIndex * blockSize + 1;
            pageEnd = Math.min(pageStart + blockSize - 1, totalPages);
            for (int i = pageStart; i <= pageEnd; i++) {
                pageNumbers.add(i);
            }
            hasPrevBlock = pageStart > 1;
            hasNextBlock = pageEnd < totalPages;
            prevBlockPage = Math.max(pageStart - 1, 1);
            nextBlockPage = Math.min(pageEnd + 1, totalPages);
        }

        model.addAttribute("pageStart", pageStart);
        model.addAttribute("pageEnd", pageEnd);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("hasPrevBlock", hasPrevBlock);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("prevBlockPage", prevBlockPage);
        model.addAttribute("nextBlockPage", nextBlockPage);
    }

    private void applyListFlags(Board board, List<Write> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        int newHours = 24;
        int hotHit = 0;
        if (board != null) {
            if (board.getBoNew() > 0) {
                newHours = board.getBoNew();
            }
            if (board.getBoHot() > 0) {
                hotHit = board.getBoHot();
            }
        }
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

    private void applyGalleryExtras(Model model, Board board, String boTable, List<Write> posts, HttpServletRequest request) {
        if (board == null || posts == null || posts.isEmpty()) {
            return;
        }
        if (!"gallery".equalsIgnoreCase(board.getBoSkin())) {
            return;
        }
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        Map<Long, String> thumbMap = new HashMap<>();
        Map<Long, String> contentMap = new HashMap<>();
        for (Write post : posts) {
            long wrId = post.getWrId();
            thumbMap.put(wrId, resolveThumbnailUrl(boTable, wrId, post.getWrContent(), contextPath));
            contentMap.put(wrId, summarizeHtml(post.getWrContent()));
        }
        model.addAttribute("thumbMap", thumbMap);
        model.addAttribute("contentMap", contentMap);
    }

    private String resolveThumbnailUrl(String boTable, long wrId, String contentHtml, String contextPath) {
        List<BoardFile> files = boardService.listFiles(boTable, wrId);
        for (BoardFile file : files) {
            String source = defaultIfBlank(file.getBfSource(), file.getBfFile());
            if (!isImageExt(source)) {
                continue;
            }
            return contextPath + "/bbs/" + boTable + "/file/" + wrId + "/" + file.getBfNo();
        }

        String inlineSrc = extractFirstImgSrc(contentHtml);
        if (!hasText(inlineSrc)) {
            return null;
        }
        String src = inlineSrc.trim();
        if (src.startsWith("http://") || src.startsWith("https://") || src.startsWith("data:") || src.startsWith("/")) {
            return src;
        }
        return contextPath + "/" + src;
    }

    private String extractFirstImgSrc(String html) {
        if (!hasText(html)) {
            return null;
        }
        String normalized = normalizeHtml(html);
        Matcher matcher = Pattern.compile("(?i)<img[^>]*\\bsrc\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>").matcher(normalized);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean isImageExt(String fileName) {
        if (!hasText(fileName)) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp")
                || lower.endsWith(".svg");
    }

    private String summarizeHtml(String html) {
        if (!hasText(html)) {
            return "";
        }
        String normalized = normalizeHtml(html);
        String plain = normalized
                .replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                .replaceAll("(?s)<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.isEmpty() && normalized.toLowerCase(Locale.ROOT).contains("<img")) {
            return "[이미지]";
        }
        if (plain.length() <= 120) {
            return plain;
        }
        return plain.substring(0, 120) + "...";
    }

    private String normalizeHtml(String html) {
        String normalized = html == null ? "" : html;
        // Some legacy rows contain multi-escaped HTML (e.g. &amp;lt;img...&amp;gt;).
        // Unescape repeatedly so gallery thumbnail/summary parsing can work reliably.
        for (int i = 0; i < 3; i++) {
            String decoded = HtmlUtils.htmlUnescape(normalized);
            if (decoded.equals(normalized)) {
                break;
            }
            normalized = decoded;
        }
        return normalized;
    }

    private String defaultIfBlank(String value, String fallback) {
        if (hasText(value)) {
            return value;
        }
        return fallback;
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

    private boolean isListable(Board board, LoginUser loginUser) {
        try {
            boardService.assertListable(board, loginUser);
            return true;
        } catch (BoardPermissionDeniedException ex) {
            return false;
        }
    }

    private LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser) {
            return (LoginUser) value;
        }
        return null;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BoardPermissionDeniedException.class)
    public Object handleBoardPermissionDenied(BoardPermissionDeniedException ex,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes,
                                              HttpSession session) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        boolean htmlRequest = accept == null || accept.contains("text/html");
        boolean fileOrApiRequest = uri.contains("/events")
                || uri.contains("/download/")
                || uri.contains("/file/");

        if (!htmlRequest || fileOrApiRequest) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        if (loginUser(session) == null) {
            return "redirect:/login";
        }

        String boTable = extractBoTable(uri);
        if (boTable != null) {
            String listPath = "/bbs/" + boTable + "/list";
            String contextPath = request.getContextPath();
            String requestPath = uri;
            if (contextPath != null && !contextPath.isBlank() && requestPath.startsWith(contextPath)) {
                requestPath = requestPath.substring(contextPath.length());
            }
            // Prevent redirect loop when the denied URL is already the board list.
            if (!listPath.equals(requestPath)) {
                return "redirect:" + listPath;
            }
        }
        return "redirect:/bbs";
    }

    private String extractBoTable(String uri) {
        if (uri == null || uri.isBlank()) {
            return null;
        }
        Matcher matcher = BBS_TABLE_PATTERN.matcher(uri);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String toIsoDateTime(String datetime) {
        if (datetime == null || datetime.isBlank()) {
            return null;
        }
        try {
            String normalized = datetime.trim().replace('T', ' ');
            normalized = normalized.replaceAll("([+-]\\d{2}:\\d{2}|Z)$", "").trim();
            if (normalized.matches("\\d{8}")) {
                normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8);
            }
            if (normalized.matches("\\d{4}-\\d{2}-\\d{2}$")) {
                return normalized;
            }
            if (normalized.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
                normalized = normalized + ":00";
            }
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(normalized,
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return dt.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    private String preferScheduleStart(Write post) {
        if (post.getWr1() != null && !post.getWr1().isBlank()) {
            return post.getWr1();
        }
        return null;
    }

    private String computeEventEnd(String start, String end, boolean allDay) {
        if (start == null || start.isBlank()) {
            return null;
        }
        if (!allDay) {
            return end;
        }
        String startDate = toDateOnly(start);
        if (startDate == null) {
            return end;
        }
        if (end == null || end.isBlank()) {
            return addOneDay(startDate);
        }
        String endDate = toDateOnly(end);
        if (endDate == null) {
            return addOneDay(startDate);
        }
        if (endDate.equals(startDate)) {
            return addOneDay(startDate);
        }
        return addOneDay(endDate);
    }

    private String toDateOnly(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace('T', ' ');
        normalized = normalized.replaceAll("([+-]\\d{2}:\\d{2}|Z)$", "").trim();
        if (normalized.matches("\\d{8}")) {
            normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8);
        }
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return normalized.substring(0, 10);
        }
        return null;
    }

    private String addOneDay(String date) {
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(date);
            return d.plusDays(1).toString();
        } catch (Exception e) {
            return date;
        }
    }

    private String toDateTimeLocal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace('T', ' ');
        normalized = normalized.replaceAll("([+-]\\d{2}:\\d{2}|Z)$", "").trim();
        if (normalized.matches("\\d{8}")) {
            normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8);
        }
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2}$")) {
            return normalized + "T00:00";
        }
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
            return normalized.substring(0, 16).replace(' ', 'T');
        }
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
            return normalized.replace(' ', 'T');
        }
        return normalized.replace(' ', 'T');
    }
}
