package com.deepcode.springboard.mail;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MailMapper {

    @Select("SELECT * FROM g5_mail WHERE ma_id = #{maId}")
    Mail findById(Integer maId);

    @Select("SELECT * FROM g5_mail ORDER BY ma_id DESC")
    List<Mail> findAll();

    @Insert("INSERT INTO g5_mail (ma_subject, ma_content, ma_time, ma_ip, ma_last) " +
            "VALUES (#{maSubject}, #{maContent}, #{maTime}, #{maIp}, #{maLast})")
    @Options(useGeneratedKeys = true, keyProperty = "maId")
    void insert(Mail mail);

    @Update("UPDATE g5_mail SET " +
            "ma_subject = #{maSubject}, " +
            "ma_content = #{maContent}, " +
            "ma_time = #{maTime}, " +
            "ma_ip = #{maIp}, " +
            "ma_last = #{maLast} " +
            "WHERE ma_id = #{maId}")
    void update(Mail mail);

    @Delete("DELETE FROM g5_mail WHERE ma_id = #{maId}")
    void delete(Integer maId);
}
