package com.deepcode.springboard.admin;

public class BoardForm {
    private String boTable;
    private String grId = "";
    private String boSubject;
    private String boSkin = "basic";
    private int boListLevel = 1;
    private int boReadLevel = 1;
    private int boWriteLevel = 2;
    private int boCommentLevel = 2;
    private int boUploadLevel = 2;
    private int boUploadCount = 2;
    private int boDownloadLevel = 1;
    private int boUseCategory = 0;
    private String boCategoryList = "";
    private int boUseSecret = 0;
    private int boUseDhtmlEditor = 0;
    private String boSelectEditor = "";
    private int boPageRows = 20;

    public String getBoTable() {
        return boTable;
    }

    public void setBoTable(String boTable) {
        this.boTable = boTable;
    }

    public String getBoSubject() {
        return boSubject;
    }

    public void setBoSubject(String boSubject) {
        this.boSubject = boSubject;
    }

    public String getGrId() {
        return grId;
    }

    public void setGrId(String grId) {
        this.grId = grId;
    }

    public String getBoSkin() {
        return boSkin;
    }

    public void setBoSkin(String boSkin) {
        this.boSkin = boSkin;
    }

    public int getBoListLevel() {
        return boListLevel;
    }

    public void setBoListLevel(int boListLevel) {
        this.boListLevel = boListLevel;
    }

    public int getBoReadLevel() {
        return boReadLevel;
    }

    public void setBoReadLevel(int boReadLevel) {
        this.boReadLevel = boReadLevel;
    }

    public int getBoWriteLevel() {
        return boWriteLevel;
    }

    public void setBoWriteLevel(int boWriteLevel) {
        this.boWriteLevel = boWriteLevel;
    }

    public int getBoCommentLevel() {
        return boCommentLevel;
    }

    public void setBoCommentLevel(int boCommentLevel) {
        this.boCommentLevel = boCommentLevel;
    }

    public int getBoUploadLevel() {
        return boUploadLevel;
    }

    public void setBoUploadLevel(int boUploadLevel) {
        this.boUploadLevel = boUploadLevel;
    }

    public int getBoUploadCount() {
        return boUploadCount;
    }

    public void setBoUploadCount(int boUploadCount) {
        this.boUploadCount = boUploadCount;
    }

    public int getBoDownloadLevel() {
        return boDownloadLevel;
    }

    public void setBoDownloadLevel(int boDownloadLevel) {
        this.boDownloadLevel = boDownloadLevel;
    }

    public int getBoUseCategory() {
        return boUseCategory;
    }

    public void setBoUseCategory(int boUseCategory) {
        this.boUseCategory = boUseCategory;
    }

    public String getBoCategoryList() {
        return boCategoryList;
    }

    public void setBoCategoryList(String boCategoryList) {
        this.boCategoryList = boCategoryList;
    }

    public int getBoUseSecret() {
        return boUseSecret;
    }

    public void setBoUseSecret(int boUseSecret) {
        this.boUseSecret = boUseSecret;
    }

    public int getBoUseDhtmlEditor() {
        return boUseDhtmlEditor;
    }

    public void setBoUseDhtmlEditor(int boUseDhtmlEditor) {
        this.boUseDhtmlEditor = boUseDhtmlEditor;
    }

    public String getBoSelectEditor() {
        return boSelectEditor;
    }

    public void setBoSelectEditor(String boSelectEditor) {
        this.boSelectEditor = boSelectEditor;
    }

    public int getBoPageRows() {
        return boPageRows;
    }

    public void setBoPageRows(int boPageRows) {
        this.boPageRows = boPageRows;
    }
}
