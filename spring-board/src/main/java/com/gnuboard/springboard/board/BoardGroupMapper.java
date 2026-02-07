package com.gnuboard.springboard.board;

import com.gnuboard.springboard.member.SearchCriteria;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BoardGroupMapper {

    @Select("SELECT * FROM g5_group WHERE gr_id = #{grId}")
    BoardGroup findById(@Param("grId") String grId);

    @Select("""
                <script>
                SELECT * FROM g5_group
                WHERE 1=1
                <if test="stx != null and stx != ''">
                    <choose>
                        <when test="sfl == 'gr_id'">
                             AND gr_id = #{stx}
                        </when>
                        <when test="sfl == 'gr_admin'">
                             AND gr_admin = #{stx}
                        </when>
                        <otherwise>
                             AND gr_subject LIKE CONCAT('%', #{stx}, '%')
                        </otherwise>
                    </choose>
                </if>
                ORDER BY ${sst} ${sod}
                LIMIT #{limit} OFFSET #{offset}
                </script>
            """)
    List<BoardGroup> findAll(SearchCriteria criteria);

    @Select("""
                <script>
                SELECT count(*) FROM g5_group
                WHERE 1=1
                <if test="stx != null and stx != ''">
                    <choose>
                        <when test="sfl == 'gr_id'">
                             AND gr_id = #{stx}
                        </when>
                        <when test="sfl == 'gr_admin'">
                             AND gr_admin = #{stx}
                        </when>
                        <otherwise>
                             AND gr_subject LIKE CONCAT('%', #{stx}, '%')
                        </otherwise>
                    </choose>
                </if>
                </script>
            """)
    int count(SearchCriteria criteria);

    @Insert("""
                INSERT INTO g5_group (
                    gr_id, gr_subject, gr_device, gr_admin, gr_use_access, gr_order,
                    gr_1_subj, gr_2_subj, gr_3_subj, gr_4_subj, gr_5_subj, gr_6_subj, gr_7_subj, gr_8_subj, gr_9_subj, gr_10_subj,
                    gr_1, gr_2, gr_3, gr_4, gr_5, gr_6, gr_7, gr_8, gr_9, gr_10
                ) VALUES (
                    #{grId}, #{grSubject}, #{grDevice}, #{grAdmin}, #{grUseAccess}, #{grOrder},
                    #{gr1Subj}, #{gr2Subj}, #{gr3Subj}, #{gr4Subj}, #{gr5Subj}, #{gr6Subj}, #{gr7Subj}, #{gr8Subj}, #{gr9Subj}, #{gr10Subj},
                    #{gr1}, #{gr2}, #{gr3}, #{gr4}, #{gr5}, #{gr6}, #{gr7}, #{gr8}, #{gr9}, #{gr10}
                )
            """)
    int insert(BoardGroup group);

    @Update("""
                UPDATE g5_group SET
                    gr_subject = #{grSubject},
                    gr_device = #{grDevice},
                    gr_admin = #{grAdmin},
                    gr_use_access = #{grUseAccess},
                    gr_order = #{grOrder},
                    gr_1_subj = #{gr1Subj}, gr_2_subj = #{gr2Subj}, gr_3_subj = #{gr3Subj}, gr_4_subj = #{gr4Subj}, gr_5_subj = #{gr5Subj},
                    gr_6_subj = #{gr6Subj}, gr_7_subj = #{gr7Subj}, gr_8_subj = #{gr8Subj}, gr_9_subj = #{gr9Subj}, gr_10_subj = #{gr10Subj},
                    gr_1 = #{gr1}, gr_2 = #{gr2}, gr_3 = #{gr3}, gr_4 = #{gr4}, gr_5 = #{gr5},
                    gr_6 = #{gr6}, gr_7 = #{gr7}, gr_8 = #{gr8}, gr_9 = #{gr9}, gr_10 = #{gr10}
                WHERE gr_id = #{grId}
            """)
    void update(BoardGroup group);

    @Delete("DELETE FROM g5_group WHERE gr_id = #{grId}")
    void delete(String grId);

    @Select("SELECT count(*) FROM g5_group_member WHERE gr_id = #{grId}")
    int countGroupMembers(@Param("grId") String grId);

    @Select("SELECT count(*) FROM g5_group_member WHERE gr_id = #{grId} AND mb_id = #{mbId}")
    int countGroupMemberByUser(@Param("grId") String grId, @Param("mbId") String mbId);

    @Select("""
            SELECT
                gm.gm_id AS gmId,
                gm.gr_id AS grId,
                gm.mb_id AS mbId,
                gm.gm_datetime AS gmDatetime,
                g.gr_subject AS grSubject
            FROM g5_group_member gm
            JOIN g5_group g ON g.gr_id = gm.gr_id
            WHERE gm.mb_id = #{mbId}
            ORDER BY gm.gm_id DESC
            """)
    List<BoardGroupMember> findGroupMembersByMemberId(@Param("mbId") String mbId);

    @Select("""
            SELECT *
            FROM g5_group
            WHERE gr_use_access = 1
            ORDER BY gr_id ASC
            """)
    List<BoardGroup> findAccessEnabledGroups();

    @Insert("""
            INSERT INTO g5_group_member (gr_id, mb_id, gm_datetime)
            VALUES (#{grId}, #{mbId}, NOW())
            """)
    int insertGroupMember(@Param("grId") String grId, @Param("mbId") String mbId);

    @Delete("""
            DELETE FROM g5_group_member
            WHERE gm_id = #{gmId}
              AND mb_id = #{mbId}
            """)
    int deleteGroupMemberById(@Param("gmId") Long gmId, @Param("mbId") String mbId);

    @Select("SELECT count(*) FROM g5_board WHERE gr_id = #{grId}")
    int countBoards(@Param("grId") String grId);

    // Helper to get board count and member count if needed... can be done via
    // separate queries or joins.
    // simpler to just do basic CRUD first.
}
