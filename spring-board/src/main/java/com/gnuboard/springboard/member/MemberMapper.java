package com.gnuboard.springboard.member;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberMapper {

    @Select("SELECT * FROM g5_member WHERE mb_id = #{mbId}")
    Member findById(@Param("mbId") String mbId);

    @Select("""
            SELECT mb_id AS mbId, mb_name AS mbName, mb_nick AS mbNick
            FROM g5_member
            ORDER BY mb_id ASC
            """)
    List<Member> findAllForSelect();

    @Select("""
            <script>
            SELECT
                m.*,
                (SELECT COUNT(*) FROM g5_group_member gm WHERE gm.mb_id = m.mb_id) AS group_count
            FROM g5_member m
            WHERE 1=1
            <if test="stx != null and stx != '' and sfl != null and sfl != ''">
                <choose>
                    <when test="sfl == 'mb_point'">
                        AND m.${sfl} <![CDATA[>=]]> #{stx}
                    </when>
                    <when test="sfl == 'mb_level'">
                        AND m.${sfl} = #{stx}
                    </when>
                    <when test="sfl == 'mb_tel' or sfl == 'mb_hp'">
                        AND m.${sfl} LIKE CONCAT('%', #{stx})
                    </when>
                    <otherwise>
                        AND m.${sfl} LIKE CONCAT(#{stx}, '%')
                    </otherwise>
                </choose>
            </if>
            ORDER BY m.${sst} ${sod}
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Member> findAll(SearchCriteria criteria);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM g5_member m
            WHERE 1=1
            <if test="stx != null and stx != '' and sfl != null and sfl != ''">
                <choose>
                    <when test="sfl == 'mb_point'">
                        AND m.${sfl} <![CDATA[>=]]> #{stx}
                    </when>
                    <when test="sfl == 'mb_level'">
                        AND m.${sfl} = #{stx}
                    </when>
                    <when test="sfl == 'mb_tel' or sfl == 'mb_hp'">
                        AND m.${sfl} LIKE CONCAT('%', #{stx})
                    </when>
                    <otherwise>
                        AND m.${sfl} LIKE CONCAT(#{stx}, '%')
                    </otherwise>
                </choose>
            </if>
            </script>
            """)
    int count(SearchCriteria criteria);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM g5_member m
            WHERE m.mb_leave_date != ''
            <if test="stx != null and stx != '' and sfl != null and sfl != ''">
                <choose>
                    <when test="sfl == 'mb_point'">
                        AND m.${sfl} <![CDATA[>=]]> #{stx}
                    </when>
                    <when test="sfl == 'mb_level'">
                        AND m.${sfl} = #{stx}
                    </when>
                    <when test="sfl == 'mb_tel' or sfl == 'mb_hp'">
                        AND m.${sfl} LIKE CONCAT('%', #{stx})
                    </when>
                    <otherwise>
                        AND m.${sfl} LIKE CONCAT(#{stx}, '%')
                    </otherwise>
                </choose>
            </if>
            </script>
            """)
    int countLeave(SearchCriteria criteria);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM g5_member m
            WHERE m.mb_intercept_date != ''
            <if test="stx != null and stx != '' and sfl != null and sfl != ''">
                <choose>
                    <when test="sfl == 'mb_point'">
                        AND m.${sfl} <![CDATA[>=]]> #{stx}
                    </when>
                    <when test="sfl == 'mb_level'">
                        AND m.${sfl} = #{stx}
                    </when>
                    <when test="sfl == 'mb_tel' or sfl == 'mb_hp'">
                        AND m.${sfl} LIKE CONCAT('%', #{stx})
                    </when>
                    <otherwise>
                        AND m.${sfl} LIKE CONCAT(#{stx}, '%')
                    </otherwise>
                </choose>
            </if>
            </script>
            """)
    int countIntercept(SearchCriteria criteria);

    @org.apache.ibatis.annotations.Insert("""
            INSERT INTO g5_member (
                mb_id,
                mb_password,
                mb_name,
                mb_nick,
                mb_nick_date,
                mb_email,
                mb_homepage,
                mb_level,
                mb_sex,
                mb_birth,
                mb_tel,
                mb_hp,
                mb_certify,
                mb_adult,
                mb_dupinfo,
                mb_zip1,
                mb_zip2,
                mb_addr1,
                mb_addr2,
                mb_addr3,
                mb_addr_jibeon,
                mb_signature,
                mb_recommend,
                mb_point,
                mb_today_login,
                mb_login_ip,
                mb_datetime,
                mb_ip,
                mb_leave_date,
                mb_intercept_date,
                mb_email_certify,
                mb_email_certify2,
                mb_memo,
                mb_lost_certify,
                mb_mailling,
                mb_sms,
                mb_open,
                mb_open_date,
                mb_profile,
                mb_memo_call,
                mb_1,
                mb_2,
                mb_3,
                mb_4,
                mb_5,
                mb_6,
                mb_7,
                mb_8,
                mb_9,
                mb_10
            ) VALUES (
                COALESCE(#{mbId}, ''),
                COALESCE(#{mbPassword}, ''),
                COALESCE(#{mbName}, ''),
                COALESCE(#{mbNick}, ''),
                COALESCE(NULLIF(#{mbNickDate}, ''), CURDATE()),
                COALESCE(#{mbEmail}, ''),
                COALESCE(#{mbHomepage}, ''),
                IFNULL(NULLIF(#{mbLevel}, 0), 2),
                COALESCE(#{mbSex}, ''),
                COALESCE(#{mbBirth}, ''),
                COALESCE(#{mbTel}, ''),
                COALESCE(#{mbHp}, ''),
                COALESCE(#{mbCertify}, ''),
                IFNULL(#{mbAdult}, 0),
                COALESCE(#{mbDupinfo}, ''),
                COALESCE(#{mbZip1}, ''),
                COALESCE(#{mbZip2}, ''),
                COALESCE(#{mbAddr1}, ''),
                COALESCE(#{mbAddr2}, ''),
                COALESCE(#{mbAddr3}, ''),
                COALESCE(#{mbAddrJibeon}, ''),
                COALESCE(#{mbSignature}, ''),
                COALESCE(#{mbRecommend}, ''),
                IFNULL(#{mbPoint}, 0),
                COALESCE(NULLIF(#{mbTodayLogin}, ''), NOW()),
                COALESCE(#{mbLoginIp}, ''),
                COALESCE(NULLIF(#{mbDatetime}, ''), NOW()),
                COALESCE(#{mbIp}, ''),
                COALESCE(#{mbLeaveDate}, ''),
                COALESCE(#{mbInterceptDate}, ''),
                COALESCE(NULLIF(#{mbEmailCertify}, ''), NOW()),
                COALESCE(#{mbEmailCertify2}, ''),
                COALESCE(#{mbMemo}, ''),
                COALESCE(#{mbLostCertify}, ''),
                IFNULL(#{mbMailling}, 0),
                IFNULL(#{mbSms}, 0),
                IFNULL(#{mbOpen}, 0),
                COALESCE(NULLIF(#{mbOpenDate}, ''), CURDATE()),
                COALESCE(#{mbProfile}, ''),
                COALESCE(#{mbMemoCall}, ''),
                COALESCE(#{mb1}, ''),
                COALESCE(#{mb2}, ''),
                COALESCE(#{mb3}, ''),
                COALESCE(#{mb4}, ''),
                COALESCE(#{mb5}, ''),
                COALESCE(#{mb6}, ''),
                COALESCE(#{mb7}, ''),
                COALESCE(#{mb8}, ''),
                COALESCE(#{mb9}, ''),
                COALESCE(#{mb10}, '')
            )
            """)
    int insertMember(Member member);

    @Update("""
            UPDATE g5_member SET
                mb_password = COALESCE(#{mbPassword}, mb_password),
                mb_name = COALESCE(#{mbName}, ''),
                mb_nick = COALESCE(#{mbNick}, ''),
                mb_nick_date = COALESCE(NULLIF(#{mbNickDate}, ''), mb_nick_date),
                mb_email = COALESCE(#{mbEmail}, ''),
                mb_homepage = COALESCE(#{mbHomepage}, ''),
                mb_level = IFNULL(#{mbLevel}, mb_level),
                mb_sex = COALESCE(#{mbSex}, ''),
                mb_birth = COALESCE(#{mbBirth}, ''),
                mb_tel = COALESCE(#{mbTel}, ''),
                mb_hp = COALESCE(#{mbHp}, ''),
                mb_certify = COALESCE(#{mbCertify}, ''),
                mb_adult = IFNULL(#{mbAdult}, 0),
                mb_dupinfo = COALESCE(#{mbDupinfo}, ''),
                mb_zip1 = COALESCE(#{mbZip1}, ''),
                mb_zip2 = COALESCE(#{mbZip2}, ''),
                mb_addr1 = COALESCE(#{mbAddr1}, ''),
                mb_addr2 = COALESCE(#{mbAddr2}, ''),
                mb_addr3 = COALESCE(#{mbAddr3}, ''),
                mb_addr_jibeon = COALESCE(#{mbAddrJibeon}, ''),
                mb_signature = COALESCE(#{mbSignature}, ''),
                mb_recommend = COALESCE(#{mbRecommend}, ''),
                mb_point = IFNULL(#{mbPoint}, 0),
                mb_leave_date = COALESCE(#{mbLeaveDate}, ''),
                mb_intercept_date = COALESCE(#{mbInterceptDate}, ''),
                mb_mailling = IFNULL(#{mbMailling}, 0),
                mb_sms = IFNULL(#{mbSms}, 0),
                mb_open = IFNULL(#{mbOpen}, 0),
                mb_profile = COALESCE(#{mbProfile}, ''),
                mb_memo = COALESCE(#{mbMemo}, ''),
                mb_1 = COALESCE(#{mb1}, ''),
                mb_2 = COALESCE(#{mb2}, ''),
                mb_3 = COALESCE(#{mb3}, ''),
                mb_4 = COALESCE(#{mb4}, ''),
                mb_5 = COALESCE(#{mb5}, ''),
                mb_6 = COALESCE(#{mb6}, ''),
                mb_7 = COALESCE(#{mb7}, ''),
                mb_8 = COALESCE(#{mb8}, ''),
                mb_9 = COALESCE(#{mb9}, ''),
                mb_10 = COALESCE(#{mb10}, '')
            WHERE mb_id = #{mbId}
            """)
    void updateMember(Member member);

    @Update("""
            UPDATE g5_member SET
                mb_mailling = #{mbMailling},
                mb_sms = #{mbSms},
                mb_open = #{mbOpen},
                mb_adult = #{mbAdult},
                mb_intercept_date = #{mbInterceptDate},
                mb_level = #{mbLevel}
            WHERE mb_id = #{mbId}
            """)
    int updateMemberFromList(Member member);

    @Delete("DELETE FROM g5_member WHERE mb_id = #{mbId}")
    void deleteMember(String mbId);
}
