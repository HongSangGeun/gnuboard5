package com.gnuboard.springboard.admin;

import com.gnuboard.springboard.bbs.domain.Board;
import com.gnuboard.springboard.bbs.mapper.BoardFileMapper;
import com.gnuboard.springboard.bbs.mapper.BoardMapper;
import com.gnuboard.springboard.bbs.service.FileStorageService;
import com.gnuboard.springboard.bbs.service.WriteTableValidator;
import com.gnuboard.springboard.common.G5Properties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class AdminBoardService {
    private final BoardMapper boardMapper;
    private final BoardFileMapper boardFileMapper;
    private final FileStorageService fileStorageService;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final G5Properties g5Properties;

    public AdminBoardService(BoardMapper boardMapper,
                             BoardFileMapper boardFileMapper,
                             FileStorageService fileStorageService,
                             JdbcTemplate jdbcTemplate,
                             ResourceLoader resourceLoader,
                             G5Properties g5Properties) {
        this.boardMapper = boardMapper;
        this.boardFileMapper = boardFileMapper;
        this.fileStorageService = fileStorageService;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.g5Properties = g5Properties;
    }

    public BoardForm toForm(Board board) {
        BoardForm form = new BoardForm();
        form.setBoTable(board.getBoTable());
        form.setGrId(board.getGrId());
        form.setBoSubject(board.getBoSubject());
        form.setBoSkin(board.getBoSkin());
        form.setBoListLevel(board.getBoListLevel());
        form.setBoReadLevel(board.getBoReadLevel());
        form.setBoWriteLevel(board.getBoWriteLevel());
        form.setBoCommentLevel(board.getBoCommentLevel());
        form.setBoUploadLevel(board.getBoUploadLevel());
        form.setBoUploadCount(board.getBoUploadCount());
        form.setBoDownloadLevel(board.getBoDownloadLevel());
        form.setBoUseCategory(board.getBoUseCategory());
        form.setBoCategoryList(board.getBoCategoryList());
        form.setBoUseSecret(board.getBoUseSecret());
        form.setBoUseDhtmlEditor(board.getBoUseDhtmlEditor());
        form.setBoSelectEditor(board.getBoSelectEditor());
        form.setBoPageRows(board.getBoPageRows());
        return form;
    }

    @Transactional
    public void create(BoardForm form) {
        String boTable = WriteTableValidator.validateBoTable(form.getBoTable());
        validate(form);
        if (boardMapper.findByTable(boTable) != null) {
            throw new IllegalArgumentException("이미 존재하는 게시판입니다.");
        }
        Board board = toBoard(form);
        boardMapper.insertBoard(board);
        createWriteTable(boTable);
    }

    @Transactional
    public void update(BoardForm form) {
        String boTable = WriteTableValidator.validateBoTable(form.getBoTable());
        validate(form);
        Board existing = boardMapper.findByTable(boTable);
        if (existing == null) {
            throw new IllegalArgumentException("게시판을 찾을 수 없습니다.");
        }
        Board board = toBoard(form);
        boardMapper.updateBoard(board);
    }

    @Transactional
    public void delete(String boTable) {
        String safe = WriteTableValidator.validateBoTable(boTable);
        Board existing = boardMapper.findByTable(safe);
        if (existing == null) {
            throw new IllegalArgumentException("게시판을 찾을 수 없습니다.");
        }
        String table = g5Properties.getWritePrefix() + safe;
        boardFileMapper.deleteByTable(safe);
        boardMapper.deleteBoard(safe);
        jdbcTemplate.execute("drop table if exists " + table);
        fileStorageService.deleteBoardDir(safe);
    }

    @Transactional
    public void copy(String sourceBoTable, String targetBoTable, String targetSubject) {
        String source = WriteTableValidator.validateBoTable(sourceBoTable);
        String target = WriteTableValidator.validateBoTable(targetBoTable);
        if (boardMapper.findByTable(target) != null) {
            throw new IllegalArgumentException("이미 존재하는 게시판입니다.");
        }
        Board sourceBoard = boardMapper.findByTable(source);
        if (sourceBoard == null) {
            throw new IllegalArgumentException("원본 게시판을 찾을 수 없습니다.");
        }
        String sourceTable = g5Properties.getWritePrefix() + source;
        int sourceWriteCount = countTableRows(sourceTable);
        int sourceFileCount = countBoardFiles(source);

        Board newBoard = toBoard(toForm(sourceBoard));
        newBoard.setBoTable(target);
        if (targetSubject != null && !targetSubject.isBlank()) {
            newBoard.setBoSubject(targetSubject);
        } else {
            newBoard.setBoSubject(sourceBoard.getBoSubject() + " (복사)");
        }
        boardMapper.insertBoard(newBoard);

        String targetTable = g5Properties.getWritePrefix() + target;
        jdbcTemplate.execute("create table " + targetTable + " like " + sourceTable);
        jdbcTemplate.execute("insert into " + targetTable + " select * from " + sourceTable);
        jdbcTemplate.update(
                "insert into g5_board_file (bo_table, wr_id, bf_no, bf_source, bf_file, bf_download, bf_content, " +
                        "bf_fileurl, bf_thumburl, bf_storage, bf_filesize, bf_width, bf_height, bf_type, bf_datetime) " +
                        "select ?, wr_id, bf_no, bf_source, bf_file, bf_download, bf_content, bf_fileurl, bf_thumburl, bf_storage, " +
                        "bf_filesize, bf_width, bf_height, bf_type, bf_datetime from g5_board_file where bo_table = ?",
                target,
                source
        );
        fileStorageService.deleteBoardDir(target);
        fileStorageService.copyBoardDir(source, target);

        int targetWriteCount = countTableRows(targetTable);
        int targetFileCount = countBoardFiles(target);
        if (targetWriteCount != sourceWriteCount || targetFileCount != sourceFileCount) {
            throw new IllegalStateException("복제된 데이터 수가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void move(String sourceBoTable, String targetBoTable) {
        String source = WriteTableValidator.validateBoTable(sourceBoTable);
        String target = WriteTableValidator.validateBoTable(targetBoTable);
        if (boardMapper.findByTable(target) != null) {
            throw new IllegalArgumentException("이미 존재하는 게시판입니다.");
        }
        Board sourceBoard = boardMapper.findByTable(source);
        if (sourceBoard == null) {
            throw new IllegalArgumentException("원본 게시판을 찾을 수 없습니다.");
        }
        String sourceTable = g5Properties.getWritePrefix() + source;
        int sourceWriteCount = countTableRows(sourceTable);
        int sourceFileCount = countBoardFiles(source);

        boardMapper.updateBoTable(source, target);
        boardFileMapper.updateBoTable(source, target);

        String targetTable = g5Properties.getWritePrefix() + target;
        jdbcTemplate.execute("rename table " + sourceTable + " to " + targetTable);
        fileStorageService.moveBoardDir(source, target);

        int targetWriteCount = countTableRows(targetTable);
        int targetFileCount = countBoardFiles(target);
        if (targetWriteCount != sourceWriteCount || targetFileCount != sourceFileCount) {
            throw new IllegalStateException("이동된 데이터 수가 일치하지 않습니다.");
        }
    }

    private Board toBoard(BoardForm form) {
        Board board = new Board();
        board.setBoTable(form.getBoTable());
        board.setGrId(form.getGrId());
        board.setBoSubject(form.getBoSubject());
        board.setBoSkin(form.getBoSkin());
        board.setBoListLevel(form.getBoListLevel());
        board.setBoReadLevel(form.getBoReadLevel());
        board.setBoWriteLevel(form.getBoWriteLevel());
        board.setBoCommentLevel(form.getBoCommentLevel());
        board.setBoUploadLevel(form.getBoUploadLevel());
        board.setBoUploadCount(form.getBoUploadCount());
        board.setBoDownloadLevel(form.getBoDownloadLevel());
        board.setBoUseCategory(form.getBoUseCategory());
        board.setBoCategoryList(form.getBoCategoryList());
        board.setBoUseSecret(form.getBoUseSecret());
        board.setBoUseDhtmlEditor(form.getBoUseDhtmlEditor());
        board.setBoSelectEditor(form.getBoSelectEditor());
        board.setBoPageRows(form.getBoPageRows());
        return board;
    }

    private void validate(BoardForm form) {
        if (form.getBoSubject() == null || form.getBoSubject().isBlank()) {
            throw new IllegalArgumentException("게시판 제목이 필요합니다.");
        }
        if (form.getBoSkin() == null || form.getBoSkin().isBlank()) {
            form.setBoSkin("basic");
        }
        if (form.getGrId() == null) {
            form.setGrId("");
        } else {
            form.setGrId(form.getGrId().trim());
        }
        form.setBoListLevel(normalizeLevel(form.getBoListLevel(), 1));
        form.setBoReadLevel(normalizeLevel(form.getBoReadLevel(), 1));
        form.setBoWriteLevel(normalizeLevel(form.getBoWriteLevel(), 2));
        form.setBoCommentLevel(normalizeLevel(form.getBoCommentLevel(), 2));
        form.setBoUploadLevel(normalizeLevel(form.getBoUploadLevel(), 2));
        form.setBoDownloadLevel(normalizeLevel(form.getBoDownloadLevel(), 1));

        if (form.getBoPageRows() <= 0) {
            form.setBoPageRows(20);
        } else if (form.getBoPageRows() > 200) {
            form.setBoPageRows(200);
        }

        if (form.getBoUploadCount() < 0) {
            form.setBoUploadCount(0);
        } else if (form.getBoUploadCount() > 50) {
            form.setBoUploadCount(50);
        }
    }

    private int normalizeLevel(int level, int defaultValue) {
        int normalized = level <= 0 ? defaultValue : level;
        if (normalized < 1) {
            return 1;
        }
        if (normalized > 10) {
            return 10;
        }
        return normalized;
    }

    private void createWriteTable(String boTable) {
        String table = g5Properties.getWritePrefix() + boTable;
        String sql = loadWriteSql();
        sql = sql.replace("__TABLE_NAME__", table);
        sql = sql.replace("CREATE TABLE `" + table + "`", "CREATE TABLE IF NOT EXISTS `" + table + "`");
        jdbcTemplate.execute(sql);
    }

    @Transactional
    public void deleteBoard(String boTable) {
        String safeBoTable = WriteTableValidator.validateBoTable(boTable);
        if (boardMapper.findByTable(safeBoTable) == null) {
            throw new IllegalArgumentException("게시판을 찾을 수 없습니다.");
        }

        // Delete metadata
        boardMapper.deleteBoard(boTable);

        // Drop table
        String table = g5Properties.getWritePrefix() + safeBoTable;
        String sql = "DROP TABLE IF EXISTS `" + table + "`";
        jdbcTemplate.execute(sql);

        // TODO: Delete files and comments if needed (or rely on admin to clean up data
        // folder)
    }

    private String loadWriteSql() {
        Resource resource = resourceLoader.getResource("classpath:sql/sql_write.sql");
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("게시글 테이블 SQL 로딩 실패", e);
        }
    }

    private int countTableRows(String table) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
        return count != null ? count : 0;
    }

    private int countBoardFiles(String boTable) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from g5_board_file where bo_table = ?",
                Integer.class,
                boTable
        );
        return count != null ? count : 0;
    }
}
