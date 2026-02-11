package com.deepcode.springboard.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * 애플리케이션 시작 시 필수 테이블이 없으면 자동 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final String[] INIT_SQL_FILES = {
            "sql/department_tables.sql",
            "sql/sms_tables.sql",
            "sql/visit_tables.sql",
            "sql/monitoring_tables.sql"
    };

    @Override
    public void run(ApplicationArguments args) {
        for (String sqlFile : INIT_SQL_FILES) {
            try {
                Resource resource = new ClassPathResource(sqlFile);
                if (!resource.exists()) {
                    continue;
                }
                Connection connection = jdbcTemplate.getDataSource().getConnection();
                try {
                    ScriptUtils.executeSqlScript(connection, resource);
                    log.info("테이블 초기화 완료: {}", sqlFile);
                } finally {
                    connection.close();
                }
            } catch (Exception e) {
                log.warn("테이블 초기화 스킵 (이미 존재하거나 오류): {} - {}", sqlFile, e.getMessage());
            }
        }

        initMinutesBoard();
    }

    /**
     * "회의록" 게시판 자동 생성 (bo_table='minutes', conference 스킨)
     */
    private void initMinutesBoard() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM g5_board WHERE bo_table = 'minutes'", Integer.class);
            if (count != null && count > 0) {
                return;
            }

            // 기본 그룹 ID 조회 (없으면 빈 문자열)
            String grId = "";
            try {
                grId = jdbcTemplate.queryForObject(
                        "SELECT gr_id FROM g5_group ORDER BY gr_id LIMIT 1", String.class);
                if (grId == null) grId = "";
            } catch (Exception ignored) {
            }

            // 회의록 게시판 레코드 생성
            jdbcTemplate.update("""
                    INSERT INTO g5_board (
                        bo_table, gr_id, bo_subject, bo_mobile_subject, bo_device, bo_admin,
                        bo_list_level, bo_read_level, bo_write_level, bo_reply_level, bo_comment_level,
                        bo_link_level, bo_upload_level, bo_download_level, bo_html_level,
                        bo_use_list_file, bo_use_list_view, bo_use_email, bo_use_cert, bo_use_sns,
                        bo_use_captcha, bo_use_good, bo_use_nogood, bo_use_name, bo_use_signature,
                        bo_use_ip_view, bo_use_list_content, bo_table_width, bo_subject_len, bo_mobile_subject_len,
                        bo_page_rows, bo_mobile_page_rows, bo_new, bo_hot, bo_image_width,
                        bo_skin, bo_mobile_skin, bo_include_head, bo_include_tail, bo_content_head,
                        bo_content_tail, bo_mobile_content_head, bo_mobile_content_tail, bo_insert_content,
                        bo_gallery_cols, bo_gallery_width, bo_gallery_height, bo_mobile_gallery_width, bo_mobile_gallery_height,
                        bo_upload_count, bo_upload_size, bo_reply_order, bo_use_search, bo_select_editor,
                        bo_use_rss_view, bo_use_sideview, bo_use_secret, bo_use_dhtml_editor, bo_use_file_content,
                        bo_write_min, bo_write_max, bo_comment_min, bo_comment_max, bo_order,
                        bo_count_write, bo_count_comment, bo_write_point, bo_read_point, bo_comment_point, bo_download_point,
                        bo_use_category, bo_category_list, bo_sort_field,
                        bo_1_subj, bo_2_subj, bo_3_subj, bo_4_subj, bo_5_subj, bo_6_subj, bo_7_subj, bo_8_subj, bo_9_subj, bo_10_subj,
                        bo_1, bo_2, bo_3, bo_4, bo_5, bo_6, bo_7, bo_8, bo_9, bo_10
                    ) VALUES (
                        'minutes', ?, '회의록', '회의록', 'both', '',
                        1, 1, 2, 2, 1,
                        2, 2, 1, 2,
                        0, 0, 0, '', 0,
                        0, 0, 0, 0, 0,
                        0, 0, 100, 60, 30,
                        15, 15, 24, 100, 835,
                        'conference', 'conference', '', '', '',
                        '', '', '', '',
                        4, 202, 150, 125, 100,
                        2, 1048576, 1, 0, 'ckeditor4',
                        0, 0, 0, 1, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0,
                        0, '', '',
                        '', '', '', '', '', '', '', '', '', '',
                        '', '', '', '', '', '', '', '', '', ''
                    )
                    """, grId);

            // g5_write_minutes 테이블 생성
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS g5_write_minutes (
                      `wr_id` int(11) NOT NULL AUTO_INCREMENT,
                      `wr_num` int(11) NOT NULL DEFAULT '0',
                      `wr_reply` varchar(10) NOT NULL,
                      `wr_parent` int(11) NOT NULL DEFAULT '0',
                      `wr_is_comment` tinyint(4) NOT NULL DEFAULT '0',
                      `wr_comment` int(11) NOT NULL DEFAULT '0',
                      `wr_comment_reply` varchar(5) NOT NULL,
                      `ca_name` varchar(255) NOT NULL,
                      `wr_option` set('html1','html2','secret','mail') NOT NULL,
                      `wr_subject` varchar(255) NOT NULL,
                      `wr_content` text NOT NULL,
                      `wr_seo_title` varchar(255) NOT NULL DEFAULT '',
                      `wr_link1` text NOT NULL,
                      `wr_link2` text NOT NULL,
                      `wr_link1_hit` int(11) NOT NULL DEFAULT '0',
                      `wr_link2_hit` int(11) NOT NULL DEFAULT '0',
                      `wr_hit` int(11) NOT NULL DEFAULT '0',
                      `wr_good` int(11) NOT NULL DEFAULT '0',
                      `wr_nogood` int(11) NOT NULL DEFAULT '0',
                      `mb_id` varchar(20) NOT NULL,
                      `wr_password` varchar(255) NOT NULL,
                      `wr_name` varchar(255) NOT NULL,
                      `wr_email` varchar(255) NOT NULL,
                      `wr_homepage` varchar(255) NOT NULL,
                      `wr_datetime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
                      `wr_file` tinyint(4) NOT NULL DEFAULT '0',
                      `wr_last` varchar(19) NOT NULL,
                      `wr_ip` varchar(255) NOT NULL,
                      `wr_facebook_user` varchar(255) NOT NULL,
                      `wr_twitter_user` varchar(255) NOT NULL,
                      `wr_1` varchar(255) NOT NULL,
                      `wr_2` varchar(255) NOT NULL,
                      `wr_3` varchar(255) NOT NULL,
                      `wr_4` varchar(255) NOT NULL,
                      `wr_5` varchar(255) NOT NULL,
                      `wr_6` varchar(255) NOT NULL,
                      `wr_7` varchar(255) NOT NULL,
                      `wr_8` varchar(255) NOT NULL,
                      `wr_9` varchar(255) NOT NULL,
                      `wr_10` varchar(255) NOT NULL,
                      PRIMARY KEY (`wr_id`),
                      KEY `wr_seo_title` (`wr_seo_title`),
                      KEY `wr_num_reply_parent` (`wr_num`,`wr_reply`,`wr_parent`),
                      KEY `wr_is_comment` (`wr_is_comment`,`wr_id`)
                    ) ENGINE=MyISAM DEFAULT CHARSET=utf8
                    """);

            log.info("회의록 게시판 자동 생성 완료 (bo_table=minutes, skin=conference)");
        } catch (Exception e) {
            log.warn("회의록 게시판 자동 생성 스킵: {}", e.getMessage());
        }
    }
}
