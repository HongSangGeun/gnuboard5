package com.gnuboard.springboard.bbs.domain;

public class Write {
    private long wrId;
    private int wrNum;
    private String wrReply;
    private long wrParent;
    private int wrIsComment;
    private int wrComment;
    private String wrCommentReply;
    private String caName;
    private String wrOption;
    private String wrSubject;
    private String wrContent;
    private String wrSeoTitle;
    private String wrLink1;
    private String wrLink2;
    private int wrLink1Hit;
    private int wrLink2Hit;
    private int wrHit;
    private int wrGood;
    private int wrNogood;
    private String mbId;
    private String wrPassword;
    private String wrName;
    private String wrEmail;
    private String wrHomepage;
    private String wrDatetime;
    private int wrFile;
    private String wrLast;
    private String wrIp;
    private String wr1;
    private String wr2;
    private String wr4;
    private String wr5;
    private boolean newPost;
    private boolean hotPost;
    private boolean fileAttached;
    private boolean linkAttached;

    public long getWrId() {
        return wrId;
    }

    public void setWrId(long wrId) {
        this.wrId = wrId;
    }

    public int getWrNum() {
        return wrNum;
    }

    public void setWrNum(int wrNum) {
        this.wrNum = wrNum;
    }

    public String getWrReply() {
        return wrReply;
    }

    public void setWrReply(String wrReply) {
        this.wrReply = wrReply;
    }

    public long getWrParent() {
        return wrParent;
    }

    public void setWrParent(long wrParent) {
        this.wrParent = wrParent;
    }

    public int getWrIsComment() {
        return wrIsComment;
    }

    public void setWrIsComment(int wrIsComment) {
        this.wrIsComment = wrIsComment;
    }

    public int getWrComment() {
        return wrComment;
    }

    public void setWrComment(int wrComment) {
        this.wrComment = wrComment;
    }

    public String getWrCommentReply() {
        return wrCommentReply;
    }

    public void setWrCommentReply(String wrCommentReply) {
        this.wrCommentReply = wrCommentReply;
    }

    public String getCaName() {
        return caName;
    }

    public void setCaName(String caName) {
        this.caName = caName;
    }

    public String getWrOption() {
        return wrOption;
    }

    public void setWrOption(String wrOption) {
        this.wrOption = wrOption;
    }

    public String getWrSubject() {
        return wrSubject;
    }

    public void setWrSubject(String wrSubject) {
        this.wrSubject = wrSubject;
    }

    public String getWrContent() {
        return wrContent;
    }

    public void setWrContent(String wrContent) {
        this.wrContent = wrContent;
    }

    public String getWrSeoTitle() {
        return wrSeoTitle;
    }

    public void setWrSeoTitle(String wrSeoTitle) {
        this.wrSeoTitle = wrSeoTitle;
    }

    public String getWrLink1() {
        return wrLink1;
    }

    public void setWrLink1(String wrLink1) {
        this.wrLink1 = wrLink1;
    }

    public String getWrLink2() {
        return wrLink2;
    }

    public void setWrLink2(String wrLink2) {
        this.wrLink2 = wrLink2;
    }

    public int getWrLink1Hit() {
        return wrLink1Hit;
    }

    public void setWrLink1Hit(int wrLink1Hit) {
        this.wrLink1Hit = wrLink1Hit;
    }

    public int getWrLink2Hit() {
        return wrLink2Hit;
    }

    public void setWrLink2Hit(int wrLink2Hit) {
        this.wrLink2Hit = wrLink2Hit;
    }

    public int getWrHit() {
        return wrHit;
    }

    public void setWrHit(int wrHit) {
        this.wrHit = wrHit;
    }

    public int getWrGood() {
        return wrGood;
    }

    public void setWrGood(int wrGood) {
        this.wrGood = wrGood;
    }

    public int getWrNogood() {
        return wrNogood;
    }

    public void setWrNogood(int wrNogood) {
        this.wrNogood = wrNogood;
    }

    public String getMbId() {
        return mbId;
    }

    public void setMbId(String mbId) {
        this.mbId = mbId;
    }

    public String getWrPassword() {
        return wrPassword;
    }

    public void setWrPassword(String wrPassword) {
        this.wrPassword = wrPassword;
    }

    public String getWrName() {
        return wrName;
    }

    public void setWrName(String wrName) {
        this.wrName = wrName;
    }

    public String getWrEmail() {
        return wrEmail;
    }

    public void setWrEmail(String wrEmail) {
        this.wrEmail = wrEmail;
    }

    public String getWrHomepage() {
        return wrHomepage;
    }

    public void setWrHomepage(String wrHomepage) {
        this.wrHomepage = wrHomepage;
    }

    public String getWrDatetime() {
        return wrDatetime;
    }

    public void setWrDatetime(String wrDatetime) {
        this.wrDatetime = wrDatetime;
    }

    public int getWrFile() {
        return wrFile;
    }

    public void setWrFile(int wrFile) {
        this.wrFile = wrFile;
    }

    public String getWr1() {
        return wr1;
    }

    public void setWr1(String wr1) {
        this.wr1 = wr1;
    }

    public String getWr2() {
        return wr2;
    }

    public void setWr2(String wr2) {
        this.wr2 = wr2;
    }

    public String getWr4() {
        return wr4;
    }

    public void setWr4(String wr4) {
        this.wr4 = wr4;
    }

    public String getWr5() {
        return wr5;
    }

    public void setWr5(String wr5) {
        this.wr5 = wr5;
    }

    public String getWrLast() {
        return wrLast;
    }

    public void setWrLast(String wrLast) {
        this.wrLast = wrLast;
    }

    public String getWrIp() {
        return wrIp;
    }

    public void setWrIp(String wrIp) {
        this.wrIp = wrIp;
    }

    public boolean isNewPost() {
        return newPost;
    }

    public void setNewPost(boolean newPost) {
        this.newPost = newPost;
    }

    public boolean isHotPost() {
        return hotPost;
    }

    public void setHotPost(boolean hotPost) {
        this.hotPost = hotPost;
    }

    public boolean isFileAttached() {
        return fileAttached;
    }

    public void setFileAttached(boolean fileAttached) {
        this.fileAttached = fileAttached;
    }

    public boolean isLinkAttached() {
        return linkAttached;
    }

    public void setLinkAttached(boolean linkAttached) {
        this.linkAttached = linkAttached;
    }
}
