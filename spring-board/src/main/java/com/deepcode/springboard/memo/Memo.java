package com.deepcode.springboard.memo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Memo {
    private Integer meId;               // 쪽지 ID
    private String meRecvMbId;          // 받는 회원 ID
    private String meSendMbId;          // 보낸 회원 ID
    private String meSendDatetime;      // 발송 시간
    private String meReadDatetime;      // 읽은 시간
    private String meMemo;              // 쪽지 내용
    private String meType;              // 유형 (recv: 받은쪽지, send: 보낸쪽지)
    private String meSendId;            // 발신 ID

    // 조회용 추가 필드
    private String mbNick;              // 닉네임
    private String mbEmail;             // 이메일
    private Boolean isRead;             // 읽음 여부
}
