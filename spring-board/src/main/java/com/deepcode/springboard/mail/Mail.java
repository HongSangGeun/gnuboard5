package com.deepcode.springboard.mail;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Mail {
    private Integer maId;           // 메일 ID
    private String maSubject;       // 메일 제목
    private String maContent;       // 메일 내용
    private LocalDateTime maTime;   // 발송 시간
    private Integer maIp;           // 발송 IP (정수형)
    private Integer maLast;         // 마지막 발송 번호
}
