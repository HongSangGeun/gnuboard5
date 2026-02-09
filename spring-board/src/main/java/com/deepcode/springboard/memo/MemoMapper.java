package com.deepcode.springboard.memo;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MemoMapper {

    // 쪽지 발송
    @Insert("INSERT INTO g5_memo (me_recv_mb_id, me_send_mb_id, me_send_datetime, me_memo, me_type, me_send_id) " +
            "VALUES (#{meRecvMbId}, #{meSendMbId}, NOW(), #{meMemo}, #{meType}, #{meSendId})")
    @Options(useGeneratedKeys = true, keyProperty = "meId")
    void insert(Memo memo);

    // 받은 쪽지 목록
    @Select("SELECT m.*, mb.mb_nick, mb.mb_email " +
            "FROM g5_memo m " +
            "LEFT JOIN g5_member mb ON m.me_send_mb_id = mb.mb_id " +
            "WHERE m.me_recv_mb_id = #{mbId} AND m.me_type = 'recv' " +
            "ORDER BY m.me_id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Memo> findReceivedMemos(@Param("mbId") String mbId,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    // 보낸 쪽지 목록
    @Select("SELECT m.*, mb.mb_nick, mb.mb_email " +
            "FROM g5_memo m " +
            "LEFT JOIN g5_member mb ON m.me_recv_mb_id = mb.mb_id " +
            "WHERE m.me_send_mb_id = #{mbId} AND m.me_type = 'send' " +
            "ORDER BY m.me_id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Memo> findSentMemos(@Param("mbId") String mbId,
                             @Param("limit") int limit,
                             @Param("offset") int offset);

    // 받은 쪽지 개수
    @Select("SELECT COUNT(*) FROM g5_memo WHERE me_recv_mb_id = #{mbId} AND me_type = 'recv'")
    int countReceivedMemos(String mbId);

    // 보낸 쪽지 개수
    @Select("SELECT COUNT(*) FROM g5_memo WHERE me_send_mb_id = #{mbId} AND me_type = 'send'")
    int countSentMemos(String mbId);

    // 읽지 않은 쪽지 개수
    @Select("SELECT COUNT(*) FROM g5_memo " +
            "WHERE me_recv_mb_id = #{mbId} AND me_type = 'recv' " +
            "AND (me_read_datetime IS NULL OR me_read_datetime = '')")
    int countUnreadMemos(String mbId);

    // 쪽지 상세 조회
    @Select("SELECT m.*, mb.mb_nick, mb.mb_email " +
            "FROM g5_memo m " +
            "LEFT JOIN g5_member mb ON " +
            "(CASE WHEN m.me_type = 'recv' THEN m.me_send_mb_id ELSE m.me_recv_mb_id END) = mb.mb_id " +
            "WHERE m.me_id = #{meId}")
    Memo findById(Integer meId);

    // 쪽지 읽음 처리
    @Update("UPDATE g5_memo SET me_read_datetime = NOW() " +
            "WHERE me_id = #{meId} AND me_recv_mb_id = #{mbId}")
    void markAsRead(@Param("meId") Integer meId, @Param("mbId") String mbId);

    // 쪽지 삭제
    @Delete("DELETE FROM g5_memo WHERE me_id = #{meId} " +
            "AND (me_recv_mb_id = #{mbId} OR me_send_mb_id = #{mbId})")
    void delete(@Param("meId") Integer meId, @Param("mbId") String mbId);

    // 쪽지 일괄 삭제
    @Delete("DELETE FROM g5_memo WHERE me_id IN " +
            "<foreach item='id' collection='meIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "AND (me_recv_mb_id = #{mbId} OR me_send_mb_id = #{mbId})")
    void deleteBatch(@Param("meIds") List<Integer> meIds, @Param("mbId") String mbId);
}
