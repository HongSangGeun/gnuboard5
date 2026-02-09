package com.deepcode.springboard.config;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface G5ConfigMapper {
    @Select("""
        select cf_admin as cfAdmin
        from g5_config
        order by cf_id
        limit 1
        """)
    G5Config load();
}
