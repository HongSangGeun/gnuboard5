package com.deepcode.springboard.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 데이터베이스 업그레이드 서비스
 * - 기존 테이블이 있으면 데이터 보존하면서 필드 추가
 * - 없는 테이블은 새로 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DbUpgradeService {

    private final JdbcTemplate jdbcTemplate;

    private static final String[] SQL_FILES = {
            "sql/gnuboard5.sql",
            "sql/visit_tables.sql",
            "sql/sms_tables.sql",
            "sql/monitoring_tables.sql"
    };

    // 업그레이드 SQL 파일 (ALTER TABLE 등)
    private static final String[] UPGRADE_SQL_FILES = {
            "sql/upgrade_bf_ocr_text_to_longtext.sql"
    };

    /**
     * DB 업그레이드 이력 테이블 생성
     */
    public void createUpgradeHistoryTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS g5_db_upgrade_history (
                    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    upgrade_type VARCHAR(50) NOT NULL COMMENT '업그레이드 타입',
                    table_name VARCHAR(100) COMMENT '테이블명',
                    description TEXT COMMENT '설명',
                    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '실행일시',
                    INDEX idx_upgrade_type (upgrade_type),
                    INDEX idx_table_name (table_name),
                    INDEX idx_executed_at (executed_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DB 업그레이드 이력'
                """;
        jdbcTemplate.execute(sql);
        log.info("DB upgrade history table created or verified");
    }

    /**
     * 현재 DB 상태 확인
     */
    public Map<String, Object> checkDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();

        // 현재 테이블 목록 조회
        List<String> existingTables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE()",
                String.class
        );

        // 동적 테이블 (게시판별 게시글 테이블 등) 분류
        List<String> dynamicTables = existingTables.stream()
                .filter(table -> table.toLowerCase().startsWith("g5_write_"))
                .collect(Collectors.toList());

        // 시스템 테이블 (동적 테이블 제외)
        List<String> systemTables = existingTables.stream()
                .filter(table -> !table.toLowerCase().startsWith("g5_write_"))
                .collect(Collectors.toList());

        status.put("existingTables", existingTables);
        status.put("totalTables", existingTables.size());
        status.put("systemTables", systemTables);
        status.put("systemTableCount", systemTables.size());
        status.put("dynamicTables", dynamicTables);
        status.put("dynamicTableCount", dynamicTables.size());

        // SQL 파일에서 정의된 테이블 목록
        Set<String> definedTables = new HashSet<>();
        for (String sqlFile : SQL_FILES) {
            try {
                Resource resource = new ClassPathResource(sqlFile);
                if (resource.exists()) {
                    definedTables.addAll(extractTableNames(resource));
                }
            } catch (Exception e) {
                log.warn("Failed to load SQL file {}: {}", sqlFile, e.getMessage());
            }
        }

        status.put("definedTables", definedTables);
        status.put("totalDefinedTables", definedTables.size());

        // 없는 테이블 찾기
        Set<String> missingTables = new HashSet<>(definedTables);
        missingTables.removeAll(existingTables.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));

        status.put("missingTables", missingTables);
        status.put("missingCount", missingTables.size());

        // 추가로 존재하는 테이블 (동적 테이블 제외)
        Set<String> extraTables = systemTables.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        extraTables.removeAll(definedTables);

        status.put("extraTables", extraTables);
        status.put("extraCount", extraTables.size());

        return status;
    }

    /**
     * DB 업그레이드 실행
     */
    @Transactional
    public List<String> executeUpgrade() {
        List<String> results = new ArrayList<>();

        // 업그레이드 이력 테이블 생성
        createUpgradeHistoryTable();
        results.add("✓ 업그레이드 이력 테이블 확인 완료");

        // 각 SQL 파일 처리
        for (String sqlFile : SQL_FILES) {
            try {
                Resource resource = new ClassPathResource(sqlFile);
                if (!resource.exists()) {
                    results.add("⚠ 파일 없음: " + sqlFile);
                    continue;
                }

                results.add("\n[" + sqlFile + "] 처리 중...");
                List<String> fileResults = processSqlFile(resource);
                results.addAll(fileResults);
            } catch (Exception e) {
                log.error("Error processing {}: {}", sqlFile, e.getMessage(), e);
                results.add("✗ 오류 발생: " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * SQL 파일 처리
     */
    private List<String> processSqlFile(Resource resource) throws IOException {
        List<String> results = new ArrayList<>();

        // ClassPathResource에서 내용 읽기
        String content;
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            content = reader.lines().collect(Collectors.joining("\n"));
        }

        // CREATE TABLE 문장들 추출
        Pattern tablePattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([`]?\\w+[`]?)\\s*\\((.*?)\\)\\s*ENGINE",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = tablePattern.matcher(content);

        while (matcher.find()) {
            String tableName = matcher.group(1).replace("`", "").trim();
            String tableDefinition = matcher.group(2);

            try {
                if (tableExists(tableName)) {
                    // 테이블이 있으면 필드 추가
                    String result = addMissingColumns(tableName, tableDefinition);
                    results.add(result);
                } else {
                    // 테이블이 없으면 생성
                    String createSql = matcher.group(0);
                    jdbcTemplate.execute(createSql);
                    results.add("✓ 테이블 생성: " + tableName);

                    // 이력 기록
                    recordUpgradeHistory("CREATE_TABLE", tableName, "테이블 생성");
                }
            } catch (Exception e) {
                log.error("Error processing table {}: {}", tableName, e.getMessage());
                results.add("✗ " + tableName + " 처리 실패: " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * 테이블 존재 여부 확인
     */
    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    /**
     * 누락된 컬럼 추가
     */
    private String addMissingColumns(String tableName, String tableDefinition) {
        // 현재 테이블의 컬럼 목록 조회
        List<String> existingColumns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                String.class,
                tableName
        );

        Set<String> existingColumnSet = existingColumns.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // 정의된 컬럼 파싱
        List<ColumnDefinition> definedColumns = parseColumnDefinitions(tableDefinition);

        int addedCount = 0;
        List<String> errors = new ArrayList<>();

        for (ColumnDefinition col : definedColumns) {
            if (!existingColumnSet.contains(col.name.toLowerCase())) {
                try {
                    String alterSql = String.format(
                            "ALTER TABLE %s ADD COLUMN IF NOT EXISTS %s %s",
                            tableName, col.name, col.definition
                    );
                    jdbcTemplate.execute(alterSql);
                    addedCount++;
                    log.info("Added column {} to table {}", col.name, tableName);
                } catch (Exception e) {
                    // IF NOT EXISTS가 지원되지 않는 경우 무시
                    try {
                        // 컬럼이 이미 있는지 확인
                        Integer colCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                                Integer.class,
                                tableName, col.name
                        );
                        if (colCount == null || colCount == 0) {
                            // 컬럼이 없으면 IF NOT EXISTS 없이 추가
                            String alterSql = String.format(
                                    "ALTER TABLE %s ADD COLUMN %s %s",
                                    tableName, col.name, col.definition
                            );
                            jdbcTemplate.execute(alterSql);
                            addedCount++;
                        }
                    } catch (Exception e2) {
                        errors.add(col.name + ": " + e2.getMessage());
                    }
                }
            }
        }

        if (addedCount > 0) {
            recordUpgradeHistory("ADD_COLUMNS", tableName, addedCount + "개 컬럼 추가");
            return String.format("✓ %s: %d개 컬럼 추가됨", tableName, addedCount);
        } else if (!errors.isEmpty()) {
            return String.format("⚠ %s: 오류 - %s", tableName, String.join(", ", errors));
        } else {
            return String.format("- %s: 변경사항 없음", tableName);
        }
    }

    /**
     * 컬럼 정의 파싱
     */
    private List<ColumnDefinition> parseColumnDefinitions(String tableDefinition) {
        List<ColumnDefinition> columns = new ArrayList<>();

        // 각 라인을 처리
        String[] lines = tableDefinition.split("\n");
        for (String line : lines) {
            line = line.trim();

            // PRIMARY KEY, INDEX, FOREIGN KEY 등은 스킵
            if (line.isEmpty() ||
                line.startsWith("PRIMARY") ||
                line.startsWith("INDEX") ||
                line.startsWith("UNIQUE") ||
                line.startsWith("KEY") ||
                line.startsWith("FOREIGN") ||
                line.startsWith("CONSTRAINT")) {
                continue;
            }

            // 컬럼 정의 추출
            // 형태: column_name TYPE ...
            Pattern columnPattern = Pattern.compile("^([`]?\\w+[`]?)\\s+(.+?)(?:,\\s*$|$)");
            Matcher matcher = columnPattern.matcher(line);

            if (matcher.find()) {
                String columnName = matcher.group(1).replace("`", "").trim();
                String columnDef = matcher.group(2).trim();
                if (columnDef.endsWith(",")) {
                    columnDef = columnDef.substring(0, columnDef.length() - 1).trim();
                }
                columns.add(new ColumnDefinition(columnName, columnDef));
            }
        }

        return columns;
    }

    /**
     * 테이블명 추출
     */
    private Set<String> extractTableNames(Resource resource) {
        Set<String> tableNames = new HashSet<>();

        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {

            String content = reader.lines().collect(Collectors.joining("\n"));
            Pattern pattern = Pattern.compile(
                    "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([`]?\\w+[`]?)",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String tableName = matcher.group(1).replace("`", "").trim();
                tableNames.add(tableName.toLowerCase());
            }
        } catch (IOException e) {
            log.error("Error reading SQL file: {}", resource.getFilename(), e);
        }

        return tableNames;
    }

    /**
     * 업그레이드 이력 기록
     */
    private void recordUpgradeHistory(String upgradeType, String tableName, String description) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO g5_db_upgrade_history (upgrade_type, table_name, description) VALUES (?, ?, ?)",
                    upgradeType, tableName, description
            );
        } catch (Exception e) {
            log.warn("Failed to record upgrade history: {}", e.getMessage());
        }
    }

    /**
     * 업그레이드 스크립트 실행 (ALTER TABLE 등)
     */
    @Transactional
    public List<String> executeUpgradeScripts() {
        List<String> results = new ArrayList<>();

        // 업그레이드 이력 테이블 생성
        createUpgradeHistoryTable();
        results.add("✓ 업그레이드 이력 테이블 확인 완료");

        if (UPGRADE_SQL_FILES.length == 0) {
            results.add("실행할 업그레이드 스크립트가 없습니다.");
            return results;
        }

        // 각 업그레이드 SQL 파일 처리
        for (String sqlFile : UPGRADE_SQL_FILES) {
            try {
                Resource resource = new ClassPathResource(sqlFile);
                if (!resource.exists()) {
                    results.add("⚠ 파일 없음: " + sqlFile);
                    continue;
                }

                results.add("\n[" + sqlFile + "] 처리 중...");
                List<String> fileResults = processUpgradeSqlFile(resource);
                results.addAll(fileResults);
            } catch (Exception e) {
                log.error("Error processing upgrade script {}: {}", sqlFile, e.getMessage(), e);
                results.add("✗ 오류 발생: " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * 업그레이드 SQL 파일 처리 (ALTER TABLE 등)
     */
    private List<String> processUpgradeSqlFile(Resource resource) throws IOException {
        List<String> results = new ArrayList<>();

        // ClassPathResource에서 내용 읽기
        String content;
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            content = reader.lines().collect(Collectors.joining("\n"));
        }

        // 주석 제거 및 SQL 문장 분리
        String[] statements = content.split(";");

        for (String statement : statements) {
            statement = statement.trim();

            // 빈 문장이나 주석만 있는 경우 스킵
            if (statement.isEmpty() || statement.startsWith("--")) {
                continue;
            }

            // 주석 제거
            String[] lines = statement.split("\n");
            StringBuilder cleanStatement = new StringBuilder();
            for (String line : lines) {
                line = line.trim();
                if (!line.startsWith("--") && !line.isEmpty()) {
                    cleanStatement.append(line).append(" ");
                }
            }

            String sql = cleanStatement.toString().trim();
            if (sql.isEmpty()) {
                continue;
            }

            try {
                // ALTER TABLE 문 파싱
                Pattern alterPattern = Pattern.compile(
                        "ALTER\\s+TABLE\\s+[`]?([\\w]+)[`]?",
                        Pattern.CASE_INSENSITIVE
                );
                Matcher matcher = alterPattern.matcher(sql);

                String tableName = null;
                if (matcher.find()) {
                    tableName = matcher.group(1);
                }

                // SQL 실행
                jdbcTemplate.execute(sql);

                if (tableName != null) {
                    results.add("✓ 테이블 수정: " + tableName);
                    recordUpgradeHistory("ALTER_TABLE", tableName, "스키마 업그레이드 실행");
                } else {
                    results.add("✓ SQL 실행 완료");
                    recordUpgradeHistory("UPGRADE_SCRIPT", null, "업그레이드 스크립트 실행");
                }

            } catch (Exception e) {
                // 이미 적용된 변경사항일 수 있음 (예: 컬럼이 이미 LONGTEXT인 경우)
                String errorMsg = e.getMessage();
                if (errorMsg != null && (
                        errorMsg.contains("Duplicate column") ||
                        errorMsg.contains("already exists") ||
                        errorMsg.contains("check that column/key exists"))) {
                    results.add("- 이미 적용됨: " + (sql.length() > 50 ? sql.substring(0, 50) + "..." : sql));
                } else {
                    log.error("Error executing upgrade SQL: {}", sql, e);
                    results.add("✗ 실행 실패: " + e.getMessage());
                }
            }
        }

        return results;
    }

    /**
     * 업그레이드 이력 조회
     */
    public List<Map<String, Object>> getUpgradeHistory(int limit) {
        // 이력 테이블이 없으면 빈 리스트 반환
        if (!tableExists("g5_db_upgrade_history")) {
            return new ArrayList<>();
        }

        try {
            return jdbcTemplate.queryForList(
                    "SELECT * FROM g5_db_upgrade_history ORDER BY executed_at DESC LIMIT ?",
                    limit
            );
        } catch (Exception e) {
            log.warn("Failed to get upgrade history: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 컬럼 정의 클래스
     */
    private static class ColumnDefinition {
        String name;
        String definition;

        ColumnDefinition(String name, String definition) {
            this.name = name;
            this.definition = definition;
        }
    }
}
