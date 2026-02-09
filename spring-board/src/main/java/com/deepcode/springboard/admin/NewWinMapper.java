package com.deepcode.springboard.admin;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface NewWinMapper {
    @Select("SELECT * FROM g5_new_win ORDER BY nw_id DESC")
    List<NewWin> findAll();

    @Select("SELECT * FROM g5_new_win WHERE nw_id = #{nwId}")
    NewWin findById(int nwId);

    @Insert("""
            INSERT INTO g5_new_win (nw_division, nw_device, nw_begin_time, nw_end_time,
                                    nw_disable_hours, nw_left, nw_top, nw_height, nw_width,
                                    nw_subject, nw_content, nw_content_html)
            VALUES (#{nwDivision}, #{nwDevice}, #{nwBeginTime}, #{nwEndTime},
                    #{nwDisableHours}, #{nwLeft}, #{nwTop}, #{nwHeight}, #{nwWidth},
                    #{nwSubject}, #{nwContent}, #{nwContentHtml})
            """)
    void insert(NewWin newWin);

    @Update("""
            UPDATE g5_new_win
            SET nw_division = #{nwDivision}, nw_device = #{nwDevice}, nw_begin_time = #{nwBeginTime},
                nw_end_time = #{nwEndTime}, nw_disable_hours = #{nwDisableHours}, nw_left = #{nwLeft},
                nw_top = #{nwTop}, nw_height = #{nwHeight}, nw_width = #{nwWidth},
                nw_subject = #{nwSubject}, nw_content = #{nwContent}, nw_content_html = #{nwContentHtml}
            WHERE nw_id = #{nwId}
            """)
    void update(NewWin newWin);

    @Delete("DELETE FROM g5_new_win WHERE nw_id = #{nwId}")
    void delete(int nwId);
}
