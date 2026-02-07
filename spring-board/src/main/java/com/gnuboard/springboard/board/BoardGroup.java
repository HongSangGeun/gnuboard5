package com.gnuboard.springboard.board;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardGroup {
    private String grId;
    private String grSubject;
    private String grDevice;
    private String grAdmin;
    private int grUseAccess;
    private int grOrder;

    // Extra fields
    private String gr1Subj;
    private String gr2Subj;
    private String gr3Subj;
    private String gr4Subj;
    private String gr5Subj;
    private String gr6Subj;
    private String gr7Subj;
    private String gr8Subj;
    private String gr9Subj;
    private String gr10Subj;

    private String gr1;
    private String gr2;
    private String gr3;
    private String gr4;
    private String gr5;
    private String gr6;
    private String gr7;
    private String gr8;
    private String gr9;
    private String gr10;

    // Transient fields for counts
    private int boardCount;
    private int memberCount;
}
