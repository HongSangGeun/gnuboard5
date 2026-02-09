package com.deepcode.springboard.sms;

import lombok.Data;

@Data
public class SmsConfig {
    private Integer cfId;
    private String cfPhone;         // 발신 번호
    private String cfIcode;         // SMS 서비스 ID
    private String cfAdmin;         // 관리자
    private String cfHp;            // 관리자 휴대폰
    private String cfEmail;         // 관리자 이메일
    private String cfSkin;          // 스킨
    private String cfDatetime;      // 최종 수정일
    private String cfIp;            // 접속 IP
    private String cfClickCount;    // 클릭 카운트
    private String cfWrite;         // 작성 권한
    private String cfReply;         // 답변 권한
    private String cfUse;           // 사용 여부
    private String cfSmsType;       // SMS 유형 (SMS, LMS, MMS)
}
