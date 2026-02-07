package com.gnuboard.springboard.bbs.mapper;

import com.gnuboard.springboard.bbs.domain.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface BoardMapper {

    @Select("""
            select *, bo_admin as boAdmin
            from g5_board
            where bo_table = #{boTable}
            """)
    Board findByTable(@Param("boTable") String boTable);

    @Select("""
            select *, bo_admin as boAdmin
            from g5_board
            order by bo_order, bo_table
            """)
    List<Board> findAll();

    @Select("""
            select *, bo_admin as boAdmin
            from g5_board
            where gr_id = #{grId}
            order by bo_order, bo_table
            """)
    List<Board> findByGroup(@Param("grId") String grId);

    @Insert("""
            insert into g5_board (
                bo_table, gr_id, bo_subject, bo_skin,
                bo_list_level, bo_read_level, bo_write_level, bo_comment_level,
                bo_upload_level, bo_upload_count, bo_download_level,
                bo_use_category, bo_category_list,
                bo_use_secret, bo_use_dhtml_editor, bo_select_editor,
                bo_page_rows,
                bo_content_head, bo_mobile_content_head,
                bo_content_tail, bo_mobile_content_tail,
                bo_insert_content, bo_notice
            ) values (
                #{boTable}, #{grId}, #{boSubject}, #{boSkin},
                #{boListLevel}, #{boReadLevel}, #{boWriteLevel}, #{boCommentLevel},
                #{boUploadLevel}, #{boUploadCount}, #{boDownloadLevel},
                #{boUseCategory}, #{boCategoryList},
                #{boUseSecret}, #{boUseDhtmlEditor}, #{boSelectEditor},
                #{boPageRows},
                '', '', '', '', '', ''
            )
            """)
    int insertBoard(Board board);

    @Update("""
            update g5_board
            set gr_id = #{grId},
                bo_subject = #{boSubject},
                bo_skin = #{boSkin},
                bo_list_level = #{boListLevel},
                bo_read_level = #{boReadLevel},
                bo_write_level = #{boWriteLevel},
                bo_comment_level = #{boCommentLevel},
                bo_upload_level = #{boUploadLevel},
                bo_upload_count = #{boUploadCount},
                bo_download_level = #{boDownloadLevel},
                bo_use_category = #{boUseCategory},
                bo_category_list = #{boCategoryList},
                bo_use_secret = #{boUseSecret},
                bo_use_dhtml_editor = #{boUseDhtmlEditor},
                bo_select_editor = #{boSelectEditor},
                bo_page_rows = #{boPageRows}
            where bo_table = #{boTable}
            """)
    int updateBoard(Board board);

    @Delete("delete from g5_board where bo_table = #{boTable}")
    int deleteBoard(@Param("boTable") String boTable);

    @Update("""
            update g5_board
            set bo_table = #{toBoTable}
            where bo_table = #{fromBoTable}
            """)
    int updateBoTable(@Param("fromBoTable") String fromBoTable, @Param("toBoTable") String toBoTable);
}
