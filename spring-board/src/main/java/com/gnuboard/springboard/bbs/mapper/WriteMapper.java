package com.gnuboard.springboard.bbs.mapper;

import com.gnuboard.springboard.bbs.domain.Write;
import com.gnuboard.springboard.bbs.domain.WriteInsert;
import com.gnuboard.springboard.bbs.domain.WriteUpdate;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

@Mapper
public interface WriteMapper {

    @SelectProvider(type = WriteTableSqlProvider.class, method = "listPosts")
    List<Write> listPosts(@Param("table") String table,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("sfl") String sfl,
            @Param("stx") String stx,
            @Param("sop") String sop,
            @Param("sca") String sca,
            @Param("sdate") String sdate,
            @Param("edate") String edate,
            @Param("sst") String sst,
            @Param("sod") String sod);

    @SelectProvider(type = WriteTableSqlProvider.class, method = "listPostsByMemberWithComments")
    List<Write> listPostsByMemberWithComments(@Param("table") String table,
            @Param("mbId") String mbId,
            @Param("limit") int limit);

    @SelectProvider(type = WriteTableSqlProvider.class, method = "countPosts")
    int countPosts(@Param("table") String table,
            @Param("sfl") String sfl,
            @Param("stx") String stx,
            @Param("sop") String sop,
            @Param("sca") String sca,
            @Param("sdate") String sdate,
            @Param("edate") String edate);

    @SelectProvider(type = WriteTableSqlProvider.class, method = "getPost")
    Write getPost(@Param("table") String table,
            @Param("wrId") long wrId);

    @SelectProvider(type = WriteTableSqlProvider.class, method = "listComments")
    List<Write> listComments(@Param("table") String table,
            @Param("wrParent") long wrParent);

    @InsertProvider(type = WriteTableSqlProvider.class, method = "insertWrite")
    @Options(useGeneratedKeys = true, keyProperty = "w.wrId")
    int insertWrite(@Param("table") String table, @Param("w") WriteInsert write);

    @UpdateProvider(type = WriteTableSqlProvider.class, method = "updateRootNumbers")
    int updateRootNumbers(@Param("table") String table,
            @Param("wrId") long wrId,
            @Param("wrNum") int wrNum,
            @Param("wrParent") long wrParent,
            @Param("wrLast") String wrLast);

    @UpdateProvider(type = WriteTableSqlProvider.class, method = "updateWrite")
    int updateWrite(@Param("table") String table,
            @Param("wrId") long wrId,
            @Param("w") WriteUpdate update);

    @DeleteProvider(type = WriteTableSqlProvider.class, method = "deleteById")
    int deleteById(@Param("table") String table, @Param("wrId") long wrId);

    @DeleteProvider(type = WriteTableSqlProvider.class, method = "deleteCommentsByParent")
    int deleteCommentsByParent(@Param("table") String table, @Param("wrParent") long wrParent);

    @UpdateProvider(type = WriteTableSqlProvider.class, method = "incrementCommentCount")
    int incrementCommentCount(@Param("table") String table, @Param("wrId") long wrId);

    @UpdateProvider(type = WriteTableSqlProvider.class, method = "decrementCommentCount")
    int decrementCommentCount(@Param("table") String table, @Param("wrId") long wrId);

    @UpdateProvider(type = WriteTableSqlProvider.class, method = "updateCommentReply")
    int updateCommentReply(@Param("table") String table,
            @Param("wrId") long wrId,
            @Param("wrCommentReply") String wrCommentReply);

    @UpdateProvider(type = WriteTableSqlProvider.class, method = "updateCommentContent")
    int updateCommentContent(@Param("table") String table,
            @Param("wrId") long wrId,
            @Param("content") String content,
            @Param("wrLast") String wrLast,
            @Param("wrIp") String wrIp);

    @UpdateProvider(type = WriteTableSqlProvider.class, method = "updateFileCount")
    int updateFileCount(@Param("table") String table,
            @Param("wrId") long wrId,
            @Param("fileCount") int fileCount);
}
