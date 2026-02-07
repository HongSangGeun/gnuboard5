package com.gnuboard.springboard.bbs.service;

import com.gnuboard.springboard.bbs.domain.Board;
import com.gnuboard.springboard.bbs.domain.BoardFile;
import com.gnuboard.springboard.bbs.domain.Write;
import com.gnuboard.springboard.bbs.domain.WriteInsert;
import com.gnuboard.springboard.bbs.domain.WriteUpdate;
import com.gnuboard.springboard.bbs.mapper.BoardFileMapper;
import com.gnuboard.springboard.bbs.mapper.BoardMapper;
import com.gnuboard.springboard.bbs.mapper.WriteMapper;
import com.gnuboard.springboard.board.BoardGroup;
import com.gnuboard.springboard.board.BoardGroupMapper;
import com.gnuboard.springboard.common.G5Properties;
import com.gnuboard.springboard.common.HtmlSanitizer;
import com.gnuboard.springboard.common.PagedResult;
import com.gnuboard.springboard.member.LoginUser;
import com.gnuboard.springboard.member.PasswordHasher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BoardService {
    private final BoardMapper boardMapper;
    private final WriteMapper writeMapper;
    private final BoardFileMapper boardFileMapper;
    private final FileStorageService fileStorageService;
    private final G5Properties g5Properties;
    private final BoardGroupMapper boardGroupMapper;
    private final JdbcTemplate jdbcTemplate;
    private final HtmlSanitizer htmlSanitizer;
    private final PasswordHasher passwordHasher = new PasswordHasher();
    private final Set<String> ensuredTables = ConcurrentHashMap.newKeySet();

    public BoardService(BoardMapper boardMapper,
            WriteMapper writeMapper,
            BoardFileMapper boardFileMapper,
            FileStorageService fileStorageService,
            G5Properties g5Properties,
            BoardGroupMapper boardGroupMapper,
            JdbcTemplate jdbcTemplate,
            HtmlSanitizer htmlSanitizer) {
        this.boardMapper = boardMapper;
        this.writeMapper = writeMapper;
        this.boardFileMapper = boardFileMapper;
        this.fileStorageService = fileStorageService;
        this.g5Properties = g5Properties;
        this.boardGroupMapper = boardGroupMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.htmlSanitizer = htmlSanitizer;
    }

    public Board getBoardOrThrow(String boTable) {
        String safeBoTable = WriteTableValidator.validateBoTable(boTable);
        Board board = boardMapper.findByTable(safeBoTable);
        if (board == null) {
            throw new IllegalArgumentException("게시판을 찾을 수 없습니다: " + safeBoTable);
        }
        return board;
    }

    public List<Board> listBoards() {
        return boardMapper.findAll();
    }

    public int getWritePages() {
        try {
            Integer value = jdbcTemplate.queryForObject(
                    "select cf_write_pages from g5_config order by cf_id limit 1",
                    Integer.class);
            if (value == null || value < 1) {
                return 10;
            }
            return Math.min(value, 50);
        } catch (Exception e) {
            return 10;
        }
    }

    public PagedResult<Write> listPosts(String boTable, int page, int size, String sfl, String stx, String sop,
            String sca, String sdate, String edate, String sst, String sod) {
        String table = resolveWriteTable(boTable);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        String safeStx = normalizeSearchText(stx);
        String safeSfl = normalizeSearchField(sfl);
        String safeSop = normalizeSearchOperator(sop);

        List<Write> posts = writeMapper.listPosts(table, offset, safeSize, safeSfl, safeStx, safeSop, sca, sdate, edate, sst, sod);
        int total = writeMapper.countPosts(table, safeSfl, safeStx, safeSop, sca, sdate, edate);
        return new PagedResult<>(posts, safePage, safeSize, total);
    }

    public List<Write> listPostsByOffset(String boTable, int offset, int size, String sfl, String stx, String sop,
            String sca, String sdate, String edate, String sst, String sod) {
        String table = resolveWriteTable(boTable);
        int safeOffset = Math.max(offset, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String safeStx = normalizeSearchText(stx);
        String safeSfl = normalizeSearchField(sfl);
        String safeSop = normalizeSearchOperator(sop);
        return writeMapper.listPosts(table, safeOffset, safeSize, safeSfl, safeStx, safeSop, sca, sdate, edate, sst, sod);
    }

    public List<Write> listPostsByMemberWithComments(String boTable, String mbId, int limit) {
        String table = resolveWriteTable(boTable);
        String safeMemberId = mbId == null ? "" : mbId.trim();
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        if (safeMemberId.isEmpty()) {
            return List.of();
        }
        return writeMapper.listPostsByMemberWithComments(table, safeMemberId, safeLimit);
    }

    public int countPosts(String boTable, String sfl, String stx, String sop, String sca, String sdate, String edate) {
        String table = resolveWriteTable(boTable);
        String safeStx = normalizeSearchText(stx);
        String safeSfl = normalizeSearchField(sfl);
        String safeSop = normalizeSearchOperator(sop);
        return writeMapper.countPosts(table, safeSfl, safeStx, safeSop, sca, sdate, edate);
    }

    public Write getPost(String boTable, long wrId) {
        String table = resolveWriteTable(boTable);
        return writeMapper.getPost(table, wrId);
    }

    public List<Write> listComments(String boTable, long wrParent) {
        String table = resolveWriteTable(boTable);
        return writeMapper.listComments(table, wrParent);
    }

    @Transactional
    public long createPost(String boTable, String subject, String content, String name, String password,
            String email, String homepage, String ip, String caName, String wr1, String wr2, String wr4, String wr5,
            LoginUser loginUser, List<MultipartFile> files) {
        validateWriteInputs(subject, content, name, password, loginUser);
        String safeSubject = htmlSanitizer.sanitizePlainText(subject);
        String safeContent = htmlSanitizer.sanitizeRichText(content);
        String safeName = htmlSanitizer.sanitizePlainText(name);
        if (safeSubject.isBlank()) {
            throw new IllegalArgumentException("제목이 필요합니다.");
        }
        if (safeContent.isBlank()) {
            throw new IllegalArgumentException("내용이 필요합니다.");
        }
        if (loginUser == null && safeName.isBlank()) {
            throw new IllegalArgumentException("작성자명이 필요합니다.");
        }

        String table = resolveWriteTable(boTable);
        ensureWriteTableSchema(table);
        String now = nowString();

        WriteInsert insert = new WriteInsert();
        insert.setWrNum(0);
        insert.setWrParent(0);
        insert.setWrIsComment(0);
        insert.setWrCommentReply("");
        insert.setMbId(loginUser != null ? loginUser.getId() : "");
        insert.setWrSubject(safeSubject);
        insert.setWrContent(safeContent);
        insert.setWrPassword(loginUser != null ? "" : encodePassword(password));
        insert.setWrName(resolveWriterName(loginUser, safeName, null));
        insert.setWrEmail(defaultString(email));
        insert.setWrHomepage(defaultString(homepage));
        insert.setWrDatetime(now);
        insert.setWrLast(now);
        insert.setWrIp(defaultString(ip));
        insert.setCaName(defaultString(caName));
        insert.setWr1(normalizeScheduleDate(wr1));
        insert.setWr2(normalizeScheduleDate(wr2));
        insert.setWr4(defaultString(wr4));
        insert.setWr5(normalizeFlag(wr5));

        writeMapper.insertWrite(table, insert);
        long wrId = insert.getWrId() != null ? insert.getWrId() : 0L;
        if (wrId > 0) {
            writeMapper.updateRootNumbers(table, wrId, (int) -wrId, wrId, now);
        }
        if (wrId > 0 && files != null && !files.isEmpty()) {
            saveFiles(boTable, wrId, files);
        }
        return wrId;
    }

    @Transactional
    public void updatePost(String boTable, long wrId, String subject, String content, String name, String password,
            String email, String homepage, String ip, String caName, String wr1, String wr2, String wr4, String wr5,
            LoginUser loginUser,
            List<MultipartFile> files, List<Integer> deleteFileNos) {
        validateWriteInputs(subject, content, name, password, loginUser);
        String safeSubject = htmlSanitizer.sanitizePlainText(subject);
        String safeContent = htmlSanitizer.sanitizeRichText(content);
        String safeName = htmlSanitizer.sanitizePlainText(name);
        if (safeSubject.isBlank()) {
            throw new IllegalArgumentException("제목이 필요합니다.");
        }
        if (safeContent.isBlank()) {
            throw new IllegalArgumentException("내용이 필요합니다.");
        }
        if (loginUser == null && safeName.isBlank()) {
            throw new IllegalArgumentException("작성자명이 필요합니다.");
        }

        String table = resolveWriteTable(boTable);
        ensureWriteTableSchema(table);
        Write existing = writeMapper.getPost(table, wrId);
        if (existing == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
        verifyPassword(existing, password, loginUser);

        WriteUpdate update = new WriteUpdate();
        update.setWrSubject(safeSubject);
        update.setWrContent(safeContent);
        update.setWrName(resolveWriterName(loginUser, safeName, existing.getWrName()));
        update.setWrEmail(defaultString(email));
        update.setWrHomepage(defaultString(homepage));
        update.setWrLast(nowString());
        update.setWrIp(defaultString(ip));
        if (caName != null) {
            update.setCaName(caName);
        }
        if (wr1 != null) {
            update.setWr1(normalizeScheduleDate(wr1));
        }
        if (wr2 != null) {
            update.setWr2(normalizeScheduleDate(wr2));
        }
        if (wr4 != null) {
            update.setWr4(wr4);
        }
        if (wr5 != null) {
            update.setWr5(normalizeFlag(wr5));
        }

        writeMapper.updateWrite(table, wrId, update);
        if (deleteFileNos != null && !deleteFileNos.isEmpty()) {
            deleteSelectedFiles(boTable, wrId, deleteFileNos);
        }
        if (files != null && !files.isEmpty()) {
            saveFiles(boTable, wrId, files);
        }
    }

    @Transactional
    public void deletePost(String boTable, long wrId, String password, LoginUser loginUser) {
        String table = resolveWriteTable(boTable);
        Write existing = writeMapper.getPost(table, wrId);
        if (existing == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
        verifyPassword(existing, password, loginUser);

        if (existing.getWrIsComment() == 1) {
            deleteFiles(boTable, wrId);
            writeMapper.deleteById(table, wrId);
            if (existing.getWrParent() > 0) {
                writeMapper.decrementCommentCount(table, existing.getWrParent());
            }
            return;
        }

        deleteFiles(boTable, wrId);
        writeMapper.deleteCommentsByParent(table, wrId);
        writeMapper.deleteById(table, wrId);
    }

    @Transactional
    public void createComment(String boTable, long parentId, String content, String name, String password, String ip,
            LoginUser loginUser, Long replyToId) {
        validateWriteInputs("comment", content, name, password, loginUser);
        String safeContent = htmlSanitizer.sanitizeRichText(content);
        String safeName = htmlSanitizer.sanitizePlainText(name);
        if (safeContent.isBlank()) {
            throw new IllegalArgumentException("내용이 필요합니다.");
        }
        if (loginUser == null && safeName.isBlank()) {
            throw new IllegalArgumentException("작성자명이 필요합니다.");
        }

        String table = resolveWriteTable(boTable);
        ensureWriteTableSchema(table);
        Write parent = writeMapper.getPost(table, parentId);
        if (parent == null || parent.getWrIsComment() == 1) {
            throw new IllegalArgumentException("댓글을 달 수 없는 글입니다.");
        }

        String now = nowString();
        WriteInsert insert = new WriteInsert();
        insert.setWrNum(parent.getWrNum());
        insert.setWrParent(parentId);
        insert.setWrIsComment(1);
        insert.setWrCommentReply(resolveCommentReplyCode(table, parentId, replyToId));
        insert.setMbId(loginUser != null ? loginUser.getId() : "");
        insert.setCaName(defaultString(parent.getCaName()));
        insert.setWr1(defaultString(parent.getWr1()));
        insert.setWr2(defaultString(parent.getWr2()));
        insert.setWr4(defaultString(parent.getWr4()));
        insert.setWr5(defaultString(parent.getWr5()));
        insert.setWrSubject("");
        insert.setWrContent(safeContent);
        insert.setWrPassword(loginUser != null ? "" : encodePassword(password));
        insert.setWrName(resolveWriterName(loginUser, safeName, null));
        insert.setWrEmail("");
        insert.setWrHomepage("");
        insert.setWrDatetime(now);
        insert.setWrLast(now);
        insert.setWrIp(defaultString(ip));

        writeMapper.insertWrite(table, insert);
        writeMapper.incrementCommentCount(table, parentId);
    }

    @Transactional
    public void updateComment(String boTable, long parentId, long commentId, String content, String password, String ip,
            LoginUser loginUser) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
        String safeContent = htmlSanitizer.sanitizeRichText(content);
        if (safeContent.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        String table = resolveWriteTable(boTable);
        Write comment = writeMapper.getPost(table, commentId);
        if (comment == null || comment.getWrIsComment() != 1 || comment.getWrParent() != parentId) {
            throw new IllegalArgumentException("댓글을 찾을 수 없습니다.");
        }

        verifyCommentPermission(comment, password, loginUser);
        writeMapper.updateCommentContent(table, commentId, safeContent, nowString(), defaultString(ip));
    }

    @Transactional
    public void deleteComment(String boTable, long parentId, long commentId, String password, LoginUser loginUser) {
        String table = resolveWriteTable(boTable);
        Write comment = writeMapper.getPost(table, commentId);
        if (comment == null || comment.getWrIsComment() != 1 || comment.getWrParent() != parentId) {
            throw new IllegalArgumentException("댓글을 찾을 수 없습니다.");
        }

        verifyCommentPermission(comment, password, loginUser);
        writeMapper.deleteById(table, commentId);
        writeMapper.decrementCommentCount(table, parentId);
    }

    private String resolveCommentReplyCode(String table, long parentPostId, Long replyToId) {
        List<Write> comments = writeMapper.listComments(table, parentPostId);
        if (replyToId == null || replyToId <= 0) {
            return nextCommentReplyCode(comments, "");
        }

        Write replyTarget = writeMapper.getPost(table, replyToId);
        if (replyTarget == null || replyTarget.getWrIsComment() != 1 || replyTarget.getWrParent() != parentPostId) {
            throw new IllegalArgumentException("답글 대상 댓글을 찾을 수 없습니다.");
        }
        String prefix = defaultString(replyTarget.getWrCommentReply());
        if (prefix.isBlank()) {
            prefix = nextCommentReplyCode(comments, "");
            writeMapper.updateCommentReply(table, replyTarget.getWrId(), prefix);
        }
        return nextCommentReplyCode(comments, prefix);
    }

    private String nextCommentReplyCode(List<Write> comments, String prefix) {
        if (prefix.length() >= 5) {
            throw new IllegalArgumentException("대댓글 최대 깊이를 초과했습니다.");
        }
        boolean[] used = new boolean[26];
        int targetLevel = prefix.length() + 1;
        for (Write comment : comments) {
            String code = defaultString(comment.getWrCommentReply());
            if (code.length() != targetLevel || !code.startsWith(prefix)) {
                continue;
            }
            char c = code.charAt(prefix.length());
            if (c >= 'A' && c <= 'Z') {
                used[c - 'A'] = true;
            }
        }
        for (int i = 0; i < 26; i++) {
            if (!used[i]) {
                return prefix + (char) ('A' + i);
            }
        }
        throw new IllegalArgumentException("해당 댓글에 더 이상 답글을 달 수 없습니다.");
    }

    public List<BoardFile> listFiles(String boTable, long wrId) {
        String safe = WriteTableValidator.validateBoTable(boTable);
        return boardFileMapper.findByPost(safe, wrId);
    }

    public BoardFile getFile(String boTable, long wrId, int bfNo) {
        String safe = WriteTableValidator.validateBoTable(boTable);
        return boardFileMapper.findOne(safe, wrId, bfNo);
    }

    public void incrementFileDownload(String boTable, long wrId, int bfNo) {
        String safe = WriteTableValidator.validateBoTable(boTable);
        boardFileMapper.incrementDownload(safe, wrId, bfNo);
    }

    public void assertReadable(Board board, LoginUser loginUser) {
        ensureGroupAccess(board, loginUser);
        ensureLevel(board.getBoReadLevel(), loginUser, "읽기 권한이 없습니다.");
    }

    public void assertListable(Board board, LoginUser loginUser) {
        ensureGroupAccess(board, loginUser);
        ensureLevel(board.getBoListLevel(), loginUser, "목록 권한이 없습니다.");
    }

    public void assertWritable(Board board, LoginUser loginUser) {
        ensureGroupAccess(board, loginUser);
        ensureLevel(board.getBoWriteLevel(), loginUser, "글쓰기 권한이 없습니다.");
    }

    public void assertCommentable(Board board, LoginUser loginUser) {
        ensureGroupAccess(board, loginUser);
        ensureLevel(board.getBoCommentLevel(), loginUser, "댓글 권한이 없습니다.");
    }

    public boolean isGroupAccessible(Board board, LoginUser loginUser) {
        try {
            ensureGroupAccess(board, loginUser);
            return true;
        } catch (BoardPermissionDeniedException ex) {
            return false;
        }
    }

    private void ensureLevel(int required, LoginUser loginUser, String message) {
        // 비로그인 사용자는 0레벨로 간주하여, 최소 1레벨 게시판은 로그인 필요
        int min = Math.max(required, 0);
        int level = loginUser != null ? loginUser.getLevel() : 0;
        if (level < min) {
            throw new BoardPermissionDeniedException(message);
        }
    }

    private void ensureGroupAccess(Board board, LoginUser loginUser) {
        if (board == null || !hasText(board.getGrId())) {
            return;
        }
        BoardGroup group = boardGroupMapper.findById(board.getGrId());
        if (group == null || group.getGrUseAccess() != 1) {
            return;
        }
        if (loginUser == null) {
            throw new BoardPermissionDeniedException("비회원은 이 게시판에 접근할 권한이 없습니다.");
        }
        if (isSuperAdmin(loginUser) || isGroupAdmin(group, loginUser)) {
            return;
        }
        int memberCount = boardGroupMapper.countGroupMemberByUser(group.getGrId(), loginUser.getId());
        if (memberCount <= 0) {
            throw new BoardPermissionDeniedException("접근 권한이 없으므로 이용할 수 없습니다.");
        }
    }

    private boolean isSuperAdmin(LoginUser loginUser) {
        return loginUser != null && loginUser.getLevel() >= 10;
    }

    private boolean isGroupAdmin(BoardGroup group, LoginUser loginUser) {
        return group != null
                && loginUser != null
                && hasText(group.getGrAdmin())
                && group.getGrAdmin().equals(loginUser.getId());
    }

    private void ensureWriteTableSchema(String table) {
        if (!ensuredTables.add(table)) {
            return;
        }
        jdbcTemplate.execute("alter table " + table + " modify wr_content longtext not null");
    }

    private void validateWriteInputs(String subject, String content, String name, String password,
            LoginUser loginUser) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("제목이 필요합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용이 필요합니다.");
        }
        if (loginUser == null && (name == null || name.isBlank())) {
            throw new IllegalArgumentException("작성자명이 필요합니다.");
        }
        if (loginUser == null && (password == null || password.isBlank())) {
            throw new IllegalArgumentException("비밀번호가 필요합니다.");
        }
    }

    private String normalizeScheduleDate(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String normalized = trimmed.replace('T', ' ');
        normalized = normalized.replaceAll("([+-]\\d{2}:\\d{2}|Z)$", "").trim();
        if (normalized.matches("\\d{8}")) {
            return normalized.substring(0, 4) + "-" + normalized.substring(4, 6) + "-" + normalized.substring(6, 8);
        }
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2}$")) {
            return normalized;
        }
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
            return normalized + ":00";
        }
        return normalized;
    }

    private String normalizeFlag(String value) {
        if (value == null) {
            return "0";
        }
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("true") || trimmed.equals("1") || trimmed.equalsIgnoreCase("yes")) {
            return "1";
        }
        return "0";
    }

    private void verifyPassword(Write existing, String password, LoginUser loginUser) {
        if (existing.getMbId() != null && !existing.getMbId().isBlank()) {
            if (loginUser == null) {
                throw new IllegalArgumentException("권한이 없습니다.");
            }
            if (isAdmin(loginUser)) {
                return;
            }
            if (!existing.getMbId().equals(loginUser.getId())) {
                throw new IllegalArgumentException("권한이 없습니다.");
            }
            return;
        }
        String stored = existing.getWrPassword();
        if (stored == null || stored.isBlank()) {
            return;
        }
        if (password == null || !passwordHasher.matches(password, stored)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    private void verifyCommentPermission(Write comment, String password, LoginUser loginUser) {
        String writerId = defaultString(comment.getMbId());
        if (!writerId.isBlank()) {
            if (loginUser == null) {
                throw new IllegalArgumentException("로그인한 작성자만 수정/삭제할 수 있습니다.");
            }
            if (isAdmin(loginUser) || writerId.equals(loginUser.getId())) {
                return;
            }
            throw new IllegalArgumentException("댓글 수정/삭제 권한이 없습니다.");
        }

        String stored = defaultString(comment.getWrPassword());
        if (stored.isBlank()) {
            throw new IllegalArgumentException("댓글 비밀번호를 확인할 수 없습니다.");
        }
        if (password == null || password.isBlank() || !passwordHasher.matches(password, stored)) {
            throw new IllegalArgumentException("댓글 비밀번호가 일치하지 않습니다.");
        }
    }

    private boolean isAdmin(LoginUser loginUser) {
        return loginUser != null && loginUser.getLevel() >= 10;
    }

    private String encodePassword(String password) {
        if (password == null || password.isBlank()) {
            return "";
        }
        return passwordHasher.createHash(password);
    }

    private void saveFiles(String boTable, long wrId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        int selectedCount = 0;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                selectedCount++;
            }
        }
        if (selectedCount == 0) {
            return;
        }

        String safeBoTable = WriteTableValidator.validateBoTable(boTable);
        Board board = boardMapper.findByTable(safeBoTable);
        int maxUploadCount = board != null ? Math.max(board.getBoUploadCount(), 0) : 0;
        if (maxUploadCount <= 0) {
            throw new IllegalArgumentException("이 게시판은 첨부파일 업로드를 사용할 수 없습니다.");
        }

        int currentCount = boardFileMapper.countByPost(safeBoTable, wrId);
        if (currentCount + selectedCount > maxUploadCount) {
            throw new IllegalArgumentException("첨부파일은 최대 " + maxUploadCount + "개까지 등록할 수 있습니다.");
        }
        int remainingSlots = maxUploadCount - currentCount;

        int nextNo = 0;
        Integer maxNo = boardFileMapper.findMaxNo(safeBoTable, wrId);
        if (maxNo != null) {
            nextNo = maxNo;
        }
        String now = nowString();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            if (remainingSlots <= 0) {
                throw new IllegalArgumentException("첨부파일은 최대 " + maxUploadCount + "개까지 등록할 수 있습니다.");
            }
            try {
                FileStorageService.StoredFile stored = fileStorageService.store(safeBoTable, file);
                if (stored == null) {
                    continue;
                }
                BoardFile bf = new BoardFile();
                bf.setBoTable(safeBoTable);
                bf.setWrId(wrId);
                bf.setBfNo(++nextNo);
                bf.setBfSource(defaultString(stored.originalName()));
                bf.setBfFile(stored.storedName());
                bf.setBfDownload(0);
                bf.setBfContent("");
                bf.setBfFileurl("");
                bf.setBfThumburl("");
                bf.setBfStorage("");
                bf.setBfFilesize(stored.size());
                bf.setBfWidth(0);
                bf.setBfHeight(0);
                bf.setBfType(0);
                bf.setBfDatetime(now);
                boardFileMapper.insertFile(bf);
                remainingSlots--;
            } catch (IOException e) {
                throw new IllegalArgumentException("파일 저장에 실패했습니다.");
            }
        }
        int count = boardFileMapper.countByPost(safeBoTable, wrId);
        String table = resolveWriteTable(boTable);
        writeMapper.updateFileCount(table, wrId, count);
    }

    private void deleteFiles(String boTable, long wrId) {
        String safeBoTable = WriteTableValidator.validateBoTable(boTable);
        List<BoardFile> files = boardFileMapper.findByPost(safeBoTable, wrId);
        for (BoardFile file : files) {
            fileStorageService.delete(safeBoTable, file.getBfFile());
        }
        boardFileMapper.deleteByPost(safeBoTable, wrId);
    }

    private void deleteSelectedFiles(String boTable, long wrId, List<Integer> deleteFileNos) {
        if (deleteFileNos == null || deleteFileNos.isEmpty()) {
            return;
        }
        String safeBoTable = WriteTableValidator.validateBoTable(boTable);
        for (Integer bfNo : deleteFileNos) {
            if (bfNo == null) {
                continue;
            }
            BoardFile file = boardFileMapper.findOne(safeBoTable, wrId, bfNo);
            if (file == null) {
                continue;
            }
            fileStorageService.delete(safeBoTable, file.getBfFile());
            boardFileMapper.deleteByNo(safeBoTable, wrId, bfNo);
        }
        int count = boardFileMapper.countByPost(safeBoTable, wrId);
        String table = resolveWriteTable(boTable);
        writeMapper.updateFileCount(table, wrId, count);
    }

    private String nowString() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveWriterName(LoginUser loginUser, String requestedName, String fallbackName) {
        if (loginUser != null) {
            if (hasText(loginUser.getName())) {
                return loginUser.getName().trim();
            }
            if (hasText(loginUser.getNick())) {
                return loginUser.getNick().trim();
            }
            if (hasText(loginUser.getId())) {
                return loginUser.getId().trim();
            }
        }
        if (hasText(requestedName)) {
            return requestedName.trim();
        }
        if (hasText(fallbackName)) {
            return fallbackName.trim();
        }
        return "익명";
    }

    private String resolveWriteTable(String boTable) {
        String safeBoTable = WriteTableValidator.validateBoTable(boTable);
        return g5Properties.getWritePrefix() + safeBoTable;
    }

    private String normalizeSearchText(String stx) {
        if (stx == null) {
            return null;
        }
        String normalized = stx.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeSearchField(String sfl) {
        if (sfl == null || sfl.isBlank()) {
            return "wr_subject";
        }
        return sfl.trim();
    }

    private String normalizeSearchOperator(String sop) {
        if ("or".equalsIgnoreCase(sop)) {
            return "or";
        }
        return "and";
    }
}
