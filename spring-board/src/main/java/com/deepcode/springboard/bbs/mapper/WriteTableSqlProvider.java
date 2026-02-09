package com.deepcode.springboard.bbs.mapper;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class WriteTableSqlProvider {

    public String listPosts(Map<String, Object> params) {
        String table = (String) params.get("table");
        String sfl = (String) params.get("sfl");
        String stx = (String) params.get("stx");
        String sop = (String) params.get("sop");
        String sca = (String) params.get("sca");
        String sdate = (String) params.get("sdate");
        String edate = (String) params.get("edate");
        String sst = (String) params.get("sst");
        String sod = (String) params.get("sod");

        // OCR 텍스트 검색 필요 여부 확인
        boolean needsOcrJoin = hasText(sfl) && sfl.contains("bf_ocr_text");
        String boTable = extractBoTable(table);

        log.debug("Search params - sfl: {}, stx: {}, needsOcrJoin: {}, boTable: {}", sfl, stx, needsOcrJoin, boTable);

        StringBuilder sql = new StringBuilder();
        boolean hasSearchFilter = hasText(stx) || hasText(sca) || hasText(sdate) || hasText(edate);
        if (hasSearchFilter) {
            sql.append("select p.* from ").append(table).append(" p ")
               .append("where p.wr_is_comment = 0 ")
               .append("and p.wr_id in (")
               .append("select distinct s.wr_parent from ").append(table).append(" s ");

            // OCR 검색 시 board_file과 JOIN
            if (needsOcrJoin) {
                sql.append("left join g5_board_file f on f.bo_table = '").append(boTable)
                   .append("' and f.wr_id = s.wr_id ");
            }

            sql.append("where 1=1");
            buildSearchCondition(sql, params, "s", sfl, stx, sop, sca, sdate, edate, needsOcrJoin);
            sql.append(")");
        } else {
            sql.append("select * from ").append(table).append(" where wr_is_comment = 0");
        }

        if (sst != null && !sst.isBlank()) {
            if (isValidSortColumn(sst)) {
                sql.append(" order by ").append(hasSearchFilter ? "p." : "").append(sst);
                if ("asc".equalsIgnoreCase(sod)) {
                    sql.append(" asc");
                } else {
                    sql.append(" desc");
                }
            } else {
                sql.append(" order by wr_num, wr_reply");
            }
        } else {
            sql.append(" order by wr_num, wr_reply");
        }

        sql.append(" limit #{size} offset #{offset}");
        String finalSql = sql.toString();
        log.debug("Generated listPosts SQL: {}", finalSql);
        return finalSql;
    }

    public String countPosts(Map<String, Object> params) {
        String table = (String) params.get("table");
        String sfl = (String) params.get("sfl");
        String stx = (String) params.get("stx");
        String sop = (String) params.get("sop");
        String sca = (String) params.get("sca");
        String sdate = (String) params.get("sdate");
        String edate = (String) params.get("edate");

        // OCR 텍스트 검색 필요 여부 확인
        boolean needsOcrJoin = hasText(sfl) && sfl.contains("bf_ocr_text");
        String boTable = extractBoTable(table);

        log.debug("Search params - sfl: {}, stx: {}, needsOcrJoin: {}, boTable: {}", sfl, stx, needsOcrJoin, boTable);

        StringBuilder sql = new StringBuilder();
        boolean hasSearchFilter = hasText(stx) || hasText(sca) || hasText(sdate) || hasText(edate);
        if (hasSearchFilter) {
            sql.append("select count(*) from ").append(table).append(" p ")
               .append("where p.wr_is_comment = 0 ")
               .append("and p.wr_id in (")
               .append("select distinct s.wr_parent from ").append(table).append(" s ");

            // OCR 검색 시 board_file과 JOIN
            if (needsOcrJoin) {
                sql.append("left join g5_board_file f on f.bo_table = '").append(boTable)
                   .append("' and f.wr_id = s.wr_id ");
            }

            sql.append("where 1=1");
            buildSearchCondition(sql, params, "s", sfl, stx, sop, sca, sdate, edate, needsOcrJoin);
            sql.append(")");
        } else {
            sql.append("select count(*) from ").append(table).append(" where wr_is_comment = 0");
        }
        String finalSql = sql.toString();
        log.debug("Generated countPosts SQL: {}", finalSql);
        return finalSql;
    }

    public String listPostsByMemberWithComments(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "select * from " + table
                + " where mb_id = #{mbId} order by wr_datetime desc, wr_id desc limit #{limit}";
    }

    private void buildSearchCondition(StringBuilder sql,
            Map<String, Object> params,
            String alias,
            String sfl,
            String stx,
            String sop,
            String sca,
            String sdate,
            String edate,
            boolean hasOcrJoin) {
        String prefix = alias + ".";
        if (hasText(sca)) {
            sql.append(" and ").append(prefix).append("ca_name = #{sca}");
        }

        SearchFieldCondition fieldCondition = parseSearchFieldCondition(sfl);
        List<String> terms = splitTerms(stx);
        String termOperator = "or".equalsIgnoreCase(sop) ? "or" : "and";
        if (!terms.isEmpty()) {
            sql.append(" and (");
            for (int i = 0; i < terms.size(); i++) {
                if (i > 0) {
                    sql.append(" ").append(termOperator).append(" ");
                }
                String paramName = "term" + i;
                params.put(paramName, terms.get(i));

                sql.append("(");
                int appended = 0;
                for (String field : fieldCondition.fields()) {
                    String safeField = normalizeSearchField(field);

                    // OCR 필드는 f 테이블에서 검색
                    if ("bf_ocr_text".equals(safeField)) {
                        if (hasOcrJoin) {
                            if (appended > 0) {
                                sql.append(" or ");
                            }
                            appendTermClause(sql, "f.", safeField, paramName);
                            appended++;
                        }
                        // hasOcrJoin이 false면 이 필드는 건너뜀
                    } else {
                        if (appended > 0) {
                            sql.append(" or ");
                        }
                        appendTermClause(sql, prefix, safeField, paramName);
                        appended++;
                    }
                }
                sql.append(")");
            }
            sql.append(")");
        }

        if ("1".equals(fieldCondition.commentMode())) {
            sql.append(" and ").append(prefix).append("wr_is_comment = '0' ");
        } else if ("0".equals(fieldCondition.commentMode())) {
            sql.append(" and ").append(prefix).append("wr_is_comment = '1' ");
        }

        if (hasText(sdate)) {
            sql.append(" and ").append(prefix).append("wr_datetime >= concat(#{sdate}, ' 00:00:00')");
        }
        if (hasText(edate)) {
            sql.append(" and ").append(prefix).append("wr_datetime <= concat(#{edate}, ' 23:59:59')");
        }
    }

    private SearchFieldCondition parseSearchFieldCondition(String sfl) {
        String defaultField = "wr_subject";
        if (!hasText(sfl)) {
            return new SearchFieldCondition(new String[] { defaultField }, null);
        }

        String[] parts = sfl.split(",");
        String fieldPart = parts.length > 0 && hasText(parts[0]) ? parts[0].trim() : defaultField;
        String[] fields = fieldPart.split("\\|\\|");
        if (fields.length == 0) {
            fields = new String[] { defaultField };
        }
        String commentMode = parts.length > 1 ? parts[1].trim() : null;
        return new SearchFieldCondition(fields, commentMode);
    }

    private List<String> splitTerms(String stx) {
        List<String> terms = new ArrayList<>();
        if (!hasText(stx)) {
            return terms;
        }
        String[] chunks = stx.trim().split("\\s+");
        for (String chunk : chunks) {
            if (hasText(chunk)) {
                terms.add(chunk.trim());
            }
        }
        return terms;
    }

    private String normalizeSearchField(String field) {
        if (!hasText(field)) {
            return "wr_subject";
        }
        String lowered = field.trim().toLowerCase(Locale.ROOT);
        if (isValidColumn(lowered)) {
            return lowered;
        }
        return "wr_subject";
    }

    private void appendTermClause(StringBuilder sql, String prefix, String field, String paramName) {
        switch (field) {
            case "mb_id":
            case "wr_name":
                sql.append(prefix).append(field).append(" = #{").append(paramName).append("}");
                break;
            case "wr_hit":
            case "wr_good":
            case "wr_nogood":
                sql.append(prefix).append(field).append(" >= cast(#{").append(paramName).append("} as signed)");
                break;
            case "wr_num":
                sql.append(prefix).append(field).append(" = (cast(#{").append(paramName).append("} as signed) * -1)");
                break;
            case "wr_ip":
            case "wr_password":
                sql.append("1 = 0");
                break;
            default:
                sql.append("INSTR(LOWER(").append(prefix).append(field).append("), LOWER(#{")
                   .append(paramName).append("})) > 0");
                break;
        }
    }

    private boolean isValidColumn(String col) {
        return col.equals("wr_subject")
                || col.equals("wr_content")
                || col.equals("wr_name")
                || col.equals("mb_id")
                || col.equals("ca_name")
                || col.equals("wr_hit")
                || col.equals("wr_good")
                || col.equals("wr_nogood")
                || col.equals("wr_num")
                || col.equals("wr_ip")
                || col.equals("wr_password")
                || col.equals("bf_ocr_text");  // OCR 텍스트 검색 지원
    }

    private boolean isValidSortColumn(String col) {
        return col.equals("wr_datetime") || col.equals("wr_hit") || col.equals("wr_good") || col.equals("wr_nogood")
                || col.equals("wr_id") || col.equals("wr_subject");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record SearchFieldCondition(String[] fields, String commentMode) {
    }

    public String getPost(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "select * from " + table + " where wr_id = #{wrId}";
    }

    public String listComments(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "select * from " + table
                + " where wr_is_comment = 1 and wr_parent = #{wrParent} order by wr_comment_reply, wr_id";
    }

    public String insertWrite(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "insert into " + table + " (" +
                "wr_num, wr_reply, wr_parent, wr_is_comment, wr_comment, wr_comment_reply, ca_name, wr_option, " +
                "wr_subject, wr_content, wr_seo_title, wr_link1, wr_link2, wr_link1_hit, wr_link2_hit, wr_hit, wr_good, wr_nogood, "
                +
                "mb_id, wr_password, wr_name, wr_email, wr_homepage, wr_datetime, wr_file, wr_last, wr_ip, " +
                "wr_facebook_user, wr_twitter_user, wr_1, wr_2, wr_3, wr_4, wr_5, wr_6, wr_7, wr_8, wr_9, wr_10" +
                ") values (" +
                "#{w.wrNum}, '', #{w.wrParent}, #{w.wrIsComment}, 0, #{w.wrCommentReply}, #{w.caName}, '', " +
                "#{w.wrSubject}, #{w.wrContent}, '', '', '', 0, 0, 0, 0, 0, " +
                "#{w.mbId}, #{w.wrPassword}, #{w.wrName}, #{w.wrEmail}, #{w.wrHomepage}, #{w.wrDatetime}, 0, #{w.wrLast}, #{w.wrIp}, "
                +
                "'', '', #{w.wr1}, #{w.wr2}, '', #{w.wr4}, #{w.wr5}, #{w.wr6}, '', '', '', ''" +
                ")";
    }

    public String updateRootNumbers(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "update " + table
                + " set wr_num = #{wrNum}, wr_parent = #{wrParent}, wr_last = #{wrLast} where wr_id = #{wrId}";
    }

    public String updateWrite(Map<String, Object> params) {
        String table = (String) params.get("table");
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(table).append(" set ");
        StringBuilder sets = new StringBuilder();
        appendSet(sets, "wr_subject = #{w.wrSubject}");
        appendSet(sets, "wr_content = #{w.wrContent}");
        appendSet(sets, "wr_name = #{w.wrName}");
        appendSet(sets, "wr_email = #{w.wrEmail}");
        appendSet(sets, "wr_homepage = #{w.wrHomepage}");
        appendSet(sets, "wr_last = #{w.wrLast}");
        appendSet(sets, "wr_ip = #{w.wrIp}");
        if (params.get("w") != null && params.get("w") instanceof com.deepcode.springboard.bbs.domain.WriteUpdate) {
            com.deepcode.springboard.bbs.domain.WriteUpdate w = (com.deepcode.springboard.bbs.domain.WriteUpdate) params.get("w");
            if (w.getCaName() != null) {
                appendSet(sets, "ca_name = #{w.caName}");
            }
            if (w.getWr1() != null) {
                appendSet(sets, "wr_1 = #{w.wr1}");
            }
            if (w.getWr2() != null) {
                appendSet(sets, "wr_2 = #{w.wr2}");
            }
            if (w.getWr4() != null) {
                appendSet(sets, "wr_4 = #{w.wr4}");
            }
            if (w.getWr5() != null) {
                appendSet(sets, "wr_5 = #{w.wr5}");
            }
            if (w.getWr6() != null) {
                appendSet(sets, "wr_6 = #{w.wr6}");
            }
        }
        sql.append(sets);
        sql.append(" where wr_id = #{wrId}");
        return sql.toString();
    }

    private void appendSet(StringBuilder sets, String clause) {
        if (sets.length() > 0) {
            sets.append(", ");
        }
        sets.append(clause);
    }

    public String deleteById(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "delete from " + table + " where wr_id = #{wrId}";
    }

    public String deleteCommentsByParent(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "delete from " + table + " where wr_is_comment = 1 and wr_parent = #{wrParent}";
    }

    public String incrementCommentCount(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "update " + table + " set wr_comment = wr_comment + 1 where wr_id = #{wrId}";
    }

    public String decrementCommentCount(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "update " + table + " set wr_comment = if(wr_comment > 0, wr_comment - 1, 0) where wr_id = #{wrId}";
    }

    public String updateCommentReply(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "update " + table + " set wr_comment_reply = #{wrCommentReply} where wr_id = #{wrId}";
    }

    public String updateCommentContent(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "update " + table + " set wr_content = #{content}, wr_last = #{wrLast}, wr_ip = #{wrIp} where wr_id = #{wrId}";
    }

    public String updateFileCount(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "update " + table + " set wr_file = #{fileCount} where wr_id = #{wrId}";
    }

    public String incrementHit(Map<String, Object> params) {
        String table = (String) params.get("table");
        return "update " + table + " set wr_hit = wr_hit + 1 where wr_id = #{wrId}";
    }

    /**
     * 테이블명에서 bo_table 추출
     * 예: "g5_write_gallery" -> "gallery"
     */
    private String extractBoTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return "";
        }
        // g5_write_ 접두사 제거
        String prefix = "g5_write_";
        if (tableName.startsWith(prefix)) {
            return tableName.substring(prefix.length());
        }
        return tableName;
    }
}
