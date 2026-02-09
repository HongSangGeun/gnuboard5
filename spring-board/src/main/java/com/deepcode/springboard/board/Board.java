package com.deepcode.springboard.board;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    private String boTable;
    private String grId;
    private String boSubject;
    private String boMobileSubject;
    private String boDevice;
    private String boAdmin;
    private String boListLevel;
    private String boReadLevel;
    private String boWriteLevel;
    private String boReplyLevel;
    private String boCommentLevel;
    private String boLinkLevel;
    private String boUploadLevel;
    private String boDownloadLevel;
    private String boHtmlLevel;
    private int boCountDelete;
    private int boCountModify;
    private String boUseListFile;
    private String boUseListView;
    private String boUseEmail;
    private String boUseCert;
    private String boUseSns;
    private String boUseCaptcha;
    private String boUseGood;
    private String boUseNogood;
    private String boUseName;
    private String boUseSignature;
    private String boUseIpView;
    private String boUseListContent; // bo_use_list_content (0/1)
    private String boTableWidth;
    private int boSubjectLen;
    private int boMobileSubjectLen;
    private int boPageRows;
    private int boMobilePageRows;
    private int boNew;
    private int boHot;
    private int boImageWidth;
    private String boSkin;
    private String boMobileSkin;
    private String boIncludeHead;
    private String boIncludeTail;
    private String boContentHead;
    private String boContentTail;
    private String boMobileContentHead;
    private String boMobileContentTail;
    private String boInsertContent;
    private int boGalleryCols;
    private int boGalleryWidth;
    private int boGalleryHeight;
    private int boMobileGalleryWidth;
    private int boMobileGalleryHeight;
    private int boUploadCount;
    private long boUploadSize;
    private int boReplyOrder;
    private int boUseSearch;
    private String boSelectEditor;
    private int boUseRssView;
    private int boUseSideview;
    private int boUseSecret;
    private int boUseDhtmlEditor;
    private int boUseFileContent;
    private int boWriteMin;
    private int boWriteMax;
    private int boCommentMin;
    private int boCommentMax;
    private int boOrder;
    private int boCountWrite;
    private int boCountComment;
    private int boWritePoint;
    private int boReadPoint;
    private int boCommentPoint;
    private int boDownloadPoint;
    private int boUseCategory;
    private String boCategoryList;
    private String boSortField;

    // Extra fields
    private String bo1Subj;
    private String bo2Subj;
    private String bo3Subj;
    private String bo4Subj;
    private String bo5Subj;
    private String bo6Subj;
    private String bo7Subj;
    private String bo8Subj;
    private String bo9Subj;
    private String bo10Subj;

    private String bo1;
    private String bo2;
    private String bo3;
    private String bo4;
    private String bo5;
    private String bo6;
    private String bo7;
    private String bo8;
    private String bo9;
    private String bo10;

    // Transient / Joins
    private String grSubject; // Group Subject name
}
