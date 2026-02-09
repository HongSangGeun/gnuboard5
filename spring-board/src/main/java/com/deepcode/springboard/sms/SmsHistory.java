package com.deepcode.springboard.sms;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SmsHistory {
    private Integer wrId;
    private String wrMessage;       // 메시지 내용
    private Integer wrTotal;        // 총 발송 수
    private Integer wrSuccess;      // 성공 수
    private Integer wrFailure;      // 실패 수
    private LocalDateTime wrDatetime; // 발송 시간
}
