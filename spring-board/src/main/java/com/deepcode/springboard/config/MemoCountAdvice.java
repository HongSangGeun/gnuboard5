package com.deepcode.springboard.config;

import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.member.LoginUser;
import com.deepcode.springboard.memo.MemoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class MemoCountAdvice {

    private final MemoService memoService;

    @ModelAttribute("memoUnreadCount")
    public Integer memoUnreadCount(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser loginUser) {
            try {
                return memoService.getUnreadMemoCount(loginUser.getId());
            } catch (Exception e) {
                log.warn("쪽지 미읽음 카운트 조회 실패: {}", e.getMessage());
                return 0;
            }
        }
        return 0;
    }
}
