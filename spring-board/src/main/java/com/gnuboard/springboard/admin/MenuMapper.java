package com.gnuboard.springboard.admin;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface MenuMapper {
    @Select("SELECT * FROM g5_menu ORDER BY me_code")
    List<Menu> findAll();

    @Select("SELECT * FROM g5_menu WHERE me_id = #{meId}")
    Menu findById(int meId);

    @Insert("""
            INSERT INTO g5_menu (me_code, me_name, me_link, me_target, me_order, me_use, me_mobile_use)
            VALUES (#{meCode}, #{meName}, #{meLink}, #{meTarget}, #{meOrder}, #{meUse}, #{meMobileUse})
            """)
    void insert(Menu menu);

    @Update("""
            UPDATE g5_menu
            SET me_code = #{meCode}, me_name = #{meName}, me_link = #{meLink},
                me_target = #{meTarget}, me_order = #{meOrder}, me_use = #{meUse},
                me_mobile_use = #{meMobileUse}
            WHERE me_id = #{meId}
            """)
    void update(Menu menu);

    @Delete("DELETE FROM g5_menu WHERE me_id = #{meId}")
    void delete(int meId);

    @Delete("DELETE FROM g5_menu WHERE me_code LIKE CONCAT(#{meCode}, '%')")
    void deleteByCodePrefix(String meCode);
}
