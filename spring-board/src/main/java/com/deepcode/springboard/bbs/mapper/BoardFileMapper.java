package com.deepcode.springboard.bbs.mapper;

import com.deepcode.springboard.bbs.domain.BoardFile;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

@Mapper
public interface BoardFileMapper {

    @Select("""
        select *
        from g5_board_file
        where bo_table = #{boTable} and wr_id = #{wrId}
        order by bf_no
        """)
    List<BoardFile> findByPost(@Param("boTable") String boTable, @Param("wrId") long wrId);

    @Select("""
        select *
        from g5_board_file
        where bo_table = #{boTable} and wr_id = #{wrId} and bf_no = #{bfNo}
        """)
    BoardFile findOne(@Param("boTable") String boTable, @Param("wrId") long wrId, @Param("bfNo") int bfNo);

    @Select("""
        select max(bf_no)
        from g5_board_file
        where bo_table = #{boTable} and wr_id = #{wrId}
        """)
    Integer findMaxNo(@Param("boTable") String boTable, @Param("wrId") long wrId);

    @Select("""
        select count(*)
        from g5_board_file
        where bo_table = #{boTable} and wr_id = #{wrId}
        """)
    int countByPost(@Param("boTable") String boTable, @Param("wrId") long wrId);

    @InsertProvider(type = BoardFileSqlProvider.class, method = "insertFile")
    int insertFile(@Param("f") BoardFile file);

    @UpdateProvider(type = BoardFileSqlProvider.class, method = "incrementDownload")
    int incrementDownload(@Param("boTable") String boTable, @Param("wrId") long wrId, @Param("bfNo") int bfNo);

    @DeleteProvider(type = BoardFileSqlProvider.class, method = "deleteByPost")
    int deleteByPost(@Param("boTable") String boTable, @Param("wrId") long wrId);

    @DeleteProvider(type = BoardFileSqlProvider.class, method = "deleteByNo")
    int deleteByNo(@Param("boTable") String boTable, @Param("wrId") long wrId, @Param("bfNo") int bfNo);

    @DeleteProvider(type = BoardFileSqlProvider.class, method = "deleteByTable")
    int deleteByTable(@Param("boTable") String boTable);

    @UpdateProvider(type = BoardFileSqlProvider.class, method = "updateBoTable")
    int updateBoTable(@Param("fromBoTable") String fromBoTable, @Param("toBoTable") String toBoTable);
}
