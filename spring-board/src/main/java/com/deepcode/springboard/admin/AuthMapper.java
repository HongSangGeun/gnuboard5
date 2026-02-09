package com.deepcode.springboard.admin;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AuthMapper {

    @Select("""
            SELECT a.*, m.mb_nick
            FROM g5_auth a
            LEFT JOIN g5_member m ON a.mb_id = m.mb_id
            ORDER BY a.mb_id, a.au_menu
            """)
    List<AuthDTO> findAll();

    @Select("SELECT * FROM g5_auth WHERE mb_id = #{mbId} AND au_menu = #{auMenu}")
    Auth findById(String mbId, String auMenu);

    @Insert("""
            INSERT INTO g5_auth (mb_id, au_menu, au_auth)
            VALUES (#{mbId}, #{auMenu}, #{auAuth})
            """)
    void insert(Auth auth);

    @Update("""
            UPDATE g5_auth
            SET au_auth = #{auAuth}
            WHERE mb_id = #{mbId} AND au_menu = #{auMenu}
            """)
    void update(Auth auth);

    @Delete("DELETE FROM g5_auth WHERE mb_id = #{mbId} AND au_menu = #{auMenu}")
    void delete(String mbId, String auMenu);

    // Helper DTO for list view
    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    class AuthDTO extends Auth {
        private String mbNick;
    }
}
