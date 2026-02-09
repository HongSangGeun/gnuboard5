package com.deepcode.springboard.health;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 모니터링 서비스 Mapper
 */
@Mapper
public interface MonitoringMapper {

    @Select("SELECT * FROM g5_monitoring_service ORDER BY ms_id DESC")
    List<MonitoringService> findAllServices();

    @Select("SELECT * FROM g5_monitoring_service WHERE ms_enabled = 1 ORDER BY ms_id")
    List<MonitoringService> findEnabledServices();

    @Select("SELECT * FROM g5_monitoring_service WHERE ms_id = #{msId}")
    MonitoringService findServiceById(@Param("msId") Integer msId);

    @Insert("""
            INSERT INTO g5_monitoring_service (
                ms_name, ms_url, ms_method, ms_auth_type, ms_auth_username,
                ms_auth_password, ms_auth_token, ms_auth_header_name, ms_auth_header_value,
                ms_timeout, ms_expected_status, ms_expected_response, ms_check_interval,
                ms_enabled, ms_alert_enabled, ms_alert_email, ms_description
            ) VALUES (
                #{msName}, #{msUrl}, #{msMethod}, #{msAuthType}, #{msAuthUsername},
                #{msAuthPassword}, #{msAuthToken}, #{msAuthHeaderName}, #{msAuthHeaderValue},
                #{msTimeout}, #{msExpectedStatus}, #{msExpectedResponse}, #{msCheckInterval},
                #{msEnabled}, #{msAlertEnabled}, #{msAlertEmail}, #{msDescription}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "msId")
    int insertService(MonitoringService service);

    @Update("""
            UPDATE g5_monitoring_service SET
                ms_name = #{msName},
                ms_url = #{msUrl},
                ms_method = #{msMethod},
                ms_auth_type = #{msAuthType},
                ms_auth_username = #{msAuthUsername},
                ms_auth_password = #{msAuthPassword},
                ms_auth_token = #{msAuthToken},
                ms_auth_header_name = #{msAuthHeaderName},
                ms_auth_header_value = #{msAuthHeaderValue},
                ms_timeout = #{msTimeout},
                ms_expected_status = #{msExpectedStatus},
                ms_expected_response = #{msExpectedResponse},
                ms_check_interval = #{msCheckInterval},
                ms_enabled = #{msEnabled},
                ms_alert_enabled = #{msAlertEnabled},
                ms_alert_email = #{msAlertEmail},
                ms_description = #{msDescription}
            WHERE ms_id = #{msId}
            """)
    int updateService(MonitoringService service);

    @Delete("DELETE FROM g5_monitoring_service WHERE ms_id = #{msId}")
    int deleteService(@Param("msId") Integer msId);

    @Insert("""
            INSERT INTO g5_monitoring_history (
                ms_id, mh_status, mh_status_code, mh_response_time, mh_cpu_usage, mh_error_message
            ) VALUES (
                #{msId}, #{mhStatus}, #{mhStatusCode}, #{mhResponseTime}, #{mhCpuUsage}, #{mhErrorMessage}
            )
            """)
    int insertHistory(MonitoringHistory history);

    @Select("""
            SELECT * FROM g5_monitoring_history
            WHERE ms_id = #{msId}
            ORDER BY mh_checked_at DESC
            LIMIT #{limit}
            """)
    List<MonitoringHistory> findHistoryByServiceId(@Param("msId") Integer msId, @Param("limit") int limit);

    @Select("""
            SELECT * FROM g5_monitoring_history
            WHERE mh_checked_at >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR)
            ORDER BY mh_checked_at DESC
            """)
    List<MonitoringHistory> findRecentHistory(@Param("hours") int hours);

    @Delete("DELETE FROM g5_monitoring_history WHERE mh_checked_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOldHistory(@Param("days") int days);

    @Select("""
            <script>
            SELECT * FROM g5_monitoring_history
            WHERE ms_id = #{msId}
            <if test="startDate != null">
                AND mh_checked_at >= #{startDate}
            </if>
            <if test="endDate != null">
                AND mh_checked_at &lt;= #{endDate}
            </if>
            ORDER BY mh_checked_at DESC
            LIMIT #{limit}
            </script>
            """)
    List<MonitoringHistory> findHistoryByDateRange(
            @Param("msId") Integer msId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("limit") int limit);
}
