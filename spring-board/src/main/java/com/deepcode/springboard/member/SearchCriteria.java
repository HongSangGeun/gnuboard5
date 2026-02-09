package com.deepcode.springboard.member;

import lombok.Data;

import java.util.Set;
import java.util.regex.Pattern;

@Data
public class SearchCriteria {

    private static final Pattern SAFE_COLUMN_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,63}$");
    private static final Set<String> ALLOWED_SOD = Set.of("asc", "desc");

    private String sfl; // search field
    private String stx; // search text
    private String sst; // sort field
    private String sod; // sort order
    private int page;
    private int limit;
    private int offset;

    public int getOffset() {
        return (page - 1) * limit;
    }

    /**
     * 정렬 필드 설정 시 화이트리스트 검증 (SQL Injection 방어)
     */
    public void setSst(String sst) {
        if (sst != null && SAFE_COLUMN_NAME.matcher(sst).matches()) {
            this.sst = sst;
        } else {
            this.sst = "mb_datetime";
        }
    }

    /**
     * 정렬 방향 설정 시 asc/desc만 허용 (SQL Injection 방어)
     */
    public void setSod(String sod) {
        if (sod != null && ALLOWED_SOD.contains(sod.toLowerCase())) {
            this.sod = sod.toLowerCase();
        } else {
            this.sod = "desc";
        }
    }

    /**
     * 검색 필드 설정 시 컬럼명 패턴 검증 (SQL Injection 방어)
     */
    public void setSfl(String sfl) {
        if (sfl != null && SAFE_COLUMN_NAME.matcher(sfl).matches()) {
            this.sfl = sfl;
        } else {
            this.sfl = null;
        }
    }
}
