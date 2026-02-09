package com.deepcode.springboard.sms;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SmsMapper {

    @Select("SELECT * FROM g5_sms5_config LIMIT 1")
    SmsConfig getConfig();

    @Update("UPDATE g5_sms5_config SET " +
            "cf_phone = #{cfPhone}, " +
            "cf_icode = #{cfIcode}, " +
            "cf_admin = #{cfAdmin}, " +
            "cf_hp = #{cfHp}, " +
            "cf_email = #{cfEmail}, " +
            "cf_use = #{cfUse}, " +
            "cf_sms_type = #{cfSmsType} " +
            "WHERE cf_id = #{cfId}")
    void updateConfig(SmsConfig config);

    @Insert("INSERT INTO g5_sms5_write (wr_message, wr_total, wr_success, wr_failure, wr_datetime) " +
            "VALUES (#{wrMessage}, #{wrTotal}, #{wrSuccess}, #{wrFailure}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "wrId")
    void insertHistory(SmsHistory history);

    @Select("SELECT * FROM g5_sms5_write ORDER BY wr_id DESC LIMIT #{limit} OFFSET #{offset}")
    List<SmsHistory> getHistory(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM g5_sms5_write")
    int getHistoryCount();
}
