package com.deepcode.springboard.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ContentMapper {
    @Select("SELECT COUNT(*) FROM g5_content")
    int countAll();

    @Select("SELECT co_id AS coId, co_subject AS coSubject FROM g5_content ORDER BY co_id LIMIT #{offset}, #{size}")
    List<Content> findPage(@Param("offset") int offset, @Param("size") int size);
}
