package com.deepcode.springboard.bbs.service;

public final class WriteTableValidator {
    private WriteTableValidator() {}

    public static String validateBoTable(String boTable) {
        if (boTable == null || boTable.isBlank()) {
            throw new IllegalArgumentException("boTable 값이 필요합니다.");
        }
        if (!boTable.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("boTable 형식이 올바르지 않습니다.");
        }
        return boTable;
    }
}
