package com.deepcode.springboard.install;

import com.deepcode.springboard.member.PasswordHasher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InstallService {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    public void install(InstallForm form) throws Exception {
        // 1. Validate Connection
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&characterEncoding=UTF-8",
                form.getDbHost(), form.getDbPort(), form.getDbName());

        try (Connection connection = DriverManager.getConnection(jdbcUrl, form.getDbUser(), form.getDbPassword())) {
            // Set MySQL mode for compatibility with legacy SQL
            try (var stmt = connection.createStatement()) {
                stmt.execute("SET SESSION sql_mode='NO_ENGINE_SUBSTITUTION'");
            }

            // 2. Run Schema Scripts (ClassPathResource works in both dev and production JAR)
            Resource gnuboardSchema = new ClassPathResource("sql/gnuboard5.sql");
            ScriptUtils.executeSqlScript(connection, gnuboardSchema);

            Resource visitTables = new ClassPathResource("sql/visit_tables.sql");
            ScriptUtils.executeSqlScript(connection, visitTables);

            Resource smsTables = new ClassPathResource("sql/sms_tables.sql");
            ScriptUtils.executeSqlScript(connection, smsTables);

            Resource monitoringTables = new ClassPathResource("sql/monitoring_tables.sql");
            ScriptUtils.executeSqlScript(connection, monitoringTables);

            // 3. Insert Admin
            insertAdmin(connection, form);

            // 4. Update application.yml
            updateApplicationYml(form, jdbcUrl);
        }
    }

    private void insertAdmin(Connection connection, InstallForm form) {
        String sql = """
                    INSERT INTO g5_member
                    (mb_id, mb_password, mb_name, mb_nick, mb_level, mb_email, mb_datetime, mb_ip, mb_email_certify, mb_open)
                    VALUES (?, ?, ?, ?, 10, ?, ?, '127.0.0.1', now(), 1)
                """;

        // Use a temporary JdbcTemplate for convenience with the single connection
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection, true);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Check if admin exists
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM g5_member WHERE mb_id = ?", Integer.class,
                form.getAdminId());
        if (count != null && count > 0) {
            return; // Already exists
        }

        String hashedPassword = passwordHasher.createHash(form.getAdminPassword());
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        jdbcTemplate.update(sql,
                form.getAdminId(),
                hashedPassword,
                form.getAdminName(),
                form.getAdminName(),
                form.getAdminEmail(),
                now);

        // Insert Config if not exists
        Integer configCount = jdbcTemplate.queryForObject("SELECT count(*) FROM g5_config", Integer.class);
        if (configCount == null || configCount == 0) {
            jdbcTemplate.update("INSERT INTO g5_config (cf_title, cf_admin) VALUES ('Gnuboard 5', ?)",
                    form.getAdminId());
        }
    }

    private void updateApplicationYml(InstallForm form, String jdbcUrl) throws IOException {
        // Warning: This updates the source file. In a real deployed jar, this wouldn't
        // persist.
        // For development/migration context, we write to
        // src/main/resources/application.yml
        Path path = Paths.get("src/main/resources/application.yml");
        if (Files.exists(path)) {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().startsWith("url:") && line.contains("jdbc:mysql")) {
                    lines.set(i, updateYamlLine(line, jdbcUrl));
                } else if (line.trim().startsWith("username:")) {
                    lines.set(i, updateYamlLine(line, form.getDbUser()));
                } else if (line.trim().startsWith("password:")) {
                    lines.set(i, updateYamlLine(line, form.getDbPassword()));
                }
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        }
    }

    private String updateYamlLine(String line, String value) {
        String key = line.split(":")[0];
        return key + ": " + value;
    }
}
