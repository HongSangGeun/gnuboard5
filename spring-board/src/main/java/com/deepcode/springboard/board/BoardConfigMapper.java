package com.deepcode.springboard.board;

import com.deepcode.springboard.member.SearchCriteria;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BoardConfigMapper {

    @Select("SELECT b.*, g.gr_subject FROM g5_board b LEFT JOIN g5_group g ON b.gr_id = g.gr_id WHERE b.bo_table = #{boTable}")
    Board findById(@Param("boTable") String boTable);

    @Select("""
                <script>
                SELECT b.*, g.gr_subject
                FROM g5_board b
                LEFT JOIN g5_group g ON b.gr_id = g.gr_id
                WHERE 1=1
                <if test="stx != null and stx != ''">
                    <choose>
                        <when test="sfl == 'bo_table'">
                             AND b.bo_table LIKE CONCAT(#{stx}, '%')
                        </when>
                        <when test="sfl == 'bo_subject'">
                             AND b.bo_subject LIKE CONCAT('%', #{stx}, '%')
                        </when>
                         <when test="sfl == 'a.gr_id'">
                             AND b.gr_id = #{stx}
                        </when>
                        <otherwise>
                             AND b.bo_subject LIKE CONCAT('%', #{stx}, '%')
                        </otherwise>
                    </choose>
                </if>
                ORDER BY ${sst} ${sod}
                LIMIT #{limit} OFFSET #{offset}
                </script>
            """)
    List<Board> findAll(SearchCriteria criteria);

    @Select("""
                <script>
                SELECT count(*)
                FROM g5_board b
                WHERE 1=1
                <if test="stx != null and stx != ''">
                    <choose>
                        <when test="sfl == 'bo_table'">
                             AND b.bo_table LIKE CONCAT(#{stx}, '%')
                        </when>
                        <when test="sfl == 'bo_subject'">
                             AND b.bo_subject LIKE CONCAT('%', #{stx}, '%')
                        </when>
                        <when test="sfl == 'a.gr_id'">
                             AND b.gr_id = #{stx}
                        </when>
                        <otherwise>
                             AND b.bo_subject LIKE CONCAT('%', #{stx}, '%')
                        </otherwise>
                    </choose>
                </if>
                </script>
            """)
    int count(SearchCriteria criteria);

    @Insert("""
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
                    #{boTable}, #{grId}, #{boSubject}, #{boMobileSubject}, #{boDevice}, #{boAdmin},
                    #{boListLevel}, #{boReadLevel}, #{boWriteLevel}, #{boReplyLevel}, #{boCommentLevel},
                    #{boLinkLevel}, #{boUploadLevel}, #{boDownloadLevel}, #{boHtmlLevel},
                    #{boUseListFile}, #{boUseListView}, #{boUseEmail}, #{boUseCert}, #{boUseSns},
                    #{boUseCaptcha}, #{boUseGood}, #{boUseNogood}, #{boUseName}, #{boUseSignature},
                    #{boUseIpView}, #{boUseListContent}, #{boTableWidth}, #{boSubjectLen}, #{boMobileSubjectLen},
                    #{boPageRows}, #{boMobilePageRows}, #{boNew}, #{boHot}, #{boImageWidth},
                    #{boSkin}, #{boMobileSkin}, #{boIncludeHead}, #{boIncludeTail}, #{boContentHead},
                    #{boContentTail}, #{boMobileContentHead}, #{boMobileContentTail}, #{boInsertContent},
                    #{boGalleryCols}, #{boGalleryWidth}, #{boGalleryHeight}, #{boMobileGalleryWidth}, #{boMobileGalleryHeight},
                    #{boUploadCount}, #{boUploadSize}, #{boReplyOrder}, #{boUseSearch}, #{boSelectEditor},
                    #{boUseRssView}, #{boUseSideview}, #{boUseSecret}, #{boUseDhtmlEditor}, #{boUseFileContent},
                    #{boWriteMin}, #{boWriteMax}, #{boCommentMin}, #{boCommentMax}, #{boOrder},
                    0, 0, #{boWritePoint}, #{boReadPoint}, #{boCommentPoint}, #{boDownloadPoint},
                     #{boUseCategory}, #{boCategoryList}, #{boSortField},
                    #{bo1Subj}, #{bo2Subj}, #{bo3Subj}, #{bo4Subj}, #{bo5Subj}, #{bo6Subj}, #{bo7Subj}, #{bo8Subj}, #{bo9Subj}, #{bo10Subj},
                    #{bo1}, #{bo2}, #{bo3}, #{bo4}, #{bo5}, #{bo6}, #{bo7}, #{bo8}, #{bo9}, #{bo10}
                )
            """)
    int insert(Board board);

    @Update("""
                UPDATE g5_board SET
                    gr_id = #{grId}, bo_subject = #{boSubject}, bo_mobile_subject = #{boMobileSubject}, bo_device = #{boDevice}, bo_admin = #{boAdmin},
                    bo_list_level = #{boListLevel}, bo_read_level = #{boReadLevel}, bo_write_level = #{boWriteLevel}, bo_reply_level = #{boReplyLevel}, bo_comment_level = #{boCommentLevel},
                    bo_link_level = #{boLinkLevel}, bo_upload_level = #{boUploadLevel}, bo_download_level = #{boDownloadLevel}, bo_html_level = #{boHtmlLevel},
                    bo_use_list_file = #{boUseListFile}, bo_use_list_view = #{boUseListView}, bo_use_email = #{boUseEmail}, bo_use_cert = #{boUseCert}, bo_use_sns = #{boUseSns},
                    bo_use_captcha = #{boUseCaptcha}, bo_use_good = #{boUseGood}, bo_use_nogood = #{boUseNogood}, bo_use_name = #{boUseName}, bo_use_signature = #{boUseSignature},
                    bo_use_ip_view = #{boUseIpView}, bo_use_list_content = #{boUseListContent}, bo_table_width = #{boTableWidth}, bo_subject_len = #{boSubjectLen}, bo_mobile_subject_len = #{boMobileSubjectLen},
                    bo_page_rows = #{boPageRows}, bo_mobile_page_rows = #{boMobilePageRows}, bo_new = #{boNew}, bo_hot = #{boHot}, bo_image_width = #{boImageWidth},
                    bo_skin = #{boSkin}, bo_mobile_skin = #{boMobileSkin}, bo_include_head = #{boIncludeHead}, bo_include_tail = #{boIncludeTail}, bo_content_head = #{boContentHead},
                    bo_content_tail = #{boContentTail}, bo_mobile_content_head = #{boMobileContentHead}, bo_mobile_content_tail = #{boMobileContentTail}, bo_insert_content = #{boInsertContent},
                    bo_gallery_cols = #{boGalleryCols}, bo_gallery_width = #{boGalleryWidth}, bo_gallery_height = #{boGalleryHeight}, bo_mobile_gallery_width = #{boMobileGalleryWidth}, bo_mobile_gallery_height = #{boMobileGalleryHeight},
                    bo_upload_count = #{boUploadCount}, bo_upload_size = #{boUploadSize}, bo_reply_order = #{boReplyOrder}, bo_use_search = #{boUseSearch}, bo_select_editor = #{boSelectEditor},
                    bo_use_rss_view = #{boUseRssView}, bo_use_sideview = #{boUseSideview}, bo_use_secret = #{boUseSecret}, bo_use_dhtml_editor = #{boUseDhtmlEditor}, bo_use_file_content = #{boUseFileContent},
                    bo_write_min = #{boWriteMin}, bo_write_max = #{boWriteMax}, bo_comment_min = #{boCommentMin}, bo_comment_max = #{boCommentMax}, bo_order = #{boOrder},
                    bo_write_point = #{boWritePoint}, bo_read_point = #{boReadPoint}, bo_comment_point = #{boCommentPoint}, bo_download_point = #{boDownloadPoint},
                    bo_use_category = #{boUseCategory}, bo_category_list = #{boCategoryList}, bo_sort_field = #{boSortField},
                    bo_1_subj = #{bo1Subj}, bo_2_subj = #{bo2Subj}, bo_3_subj = #{bo3Subj}, bo_4_subj = #{bo4Subj}, bo_5_subj = #{bo5Subj},
                    bo_6_subj = #{bo6Subj}, bo_7_subj = #{bo7Subj}, bo_8_subj = #{bo8Subj}, bo_9_subj = #{bo9Subj}, bo_10_subj = #{bo10Subj},
                    bo_1 = #{bo1}, bo_2 = #{bo2}, bo_3 = #{bo3}, bo_4 = #{bo4}, bo_5 = #{bo5},
                    bo_6 = #{bo6}, bo_7 = #{bo7}, bo_8 = #{bo8}, bo_9 = #{bo9}, bo_10 = #{bo10}
                WHERE bo_table = #{boTable}
            """)
    void update(Board board);

    @Delete("DELETE FROM g5_board WHERE bo_table = #{boTable}")
    void delete(String boTable);

    @Update("""
                CREATE TABLE g5_write_${boTable} (
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
            """)
    void createWriteTable(@Param("boTable") String boTable);
}
