package com.deepcode.springboard.stats;

import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StatsMapper {

    // 인기 검색어 저장
    @Insert("INSERT INTO g5_popular (pp_word, pp_date, pp_ip) VALUES (#{ppWord}, #{ppDate}, #{ppIp})")
    void insertPopularKeyword(PopularKeyword keyword);

    // 인기 검색어 목록
    @Select("SELECT * FROM g5_popular ORDER BY pp_id DESC LIMIT #{limit} OFFSET #{offset}")
    List<PopularKeyword> getPopularKeywords(@Param("limit") int limit, @Param("offset") int offset);

    // 인기 검색어 개수
    @Select("SELECT COUNT(*) FROM g5_popular")
    int getPopularKeywordCount();

    // 인기 검색어 순위 (기간별)
    @Select("SELECT pp_word, COUNT(*) as cnt FROM g5_popular " +
            "WHERE pp_date BETWEEN #{fromDate} AND #{toDate} " +
            "AND TRIM(pp_word) <> '' " +
            "GROUP BY pp_word " +
            "ORDER BY cnt DESC " +
            "LIMIT #{limit}")
    List<PopularKeywordRank> getPopularKeywordRank(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("limit") int limit);

    // 인기 검색어 삭제
    @Delete("DELETE FROM g5_popular WHERE pp_id = #{ppId}")
    void deletePopularKeyword(Integer ppId);

    // 인기 검색어 일괄 삭제
    @Delete("DELETE FROM g5_popular WHERE pp_date < #{beforeDate}")
    void deletePopularKeywordsBefore(LocalDate beforeDate);
}
