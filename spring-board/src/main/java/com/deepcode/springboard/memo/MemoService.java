package com.deepcode.springboard.memo;

import com.deepcode.springboard.member.Member;
import com.deepcode.springboard.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoMapper memoMapper;
    private final MemberMapper memberMapper;

    /**
     * 쪽지 발송
     */
    @Transactional
    public Memo sendMemo(String fromMbId, String toMbId, String content) {
        // 받는 회원 확인
        Member receiver = memberMapper.findById(toMbId);
        if (receiver == null) {
            throw new IllegalArgumentException("받는 회원을 찾을 수 없습니다.");
        }

        // 발신 ID 생성
        String sendId = UUID.randomUUID().toString().replace("-", "");

        // 받는 사람 쪽지함에 저장 (recv)
        Memo recvMemo = new Memo();
        recvMemo.setMeRecvMbId(toMbId);
        recvMemo.setMeSendMbId(fromMbId);
        recvMemo.setMeMemo(content);
        recvMemo.setMeType("recv");
        recvMemo.setMeSendId(sendId);
        memoMapper.insert(recvMemo);

        // 보낸 사람 쪽지함에 저장 (send)
        Memo sendMemo = new Memo();
        sendMemo.setMeRecvMbId(toMbId);
        sendMemo.setMeSendMbId(fromMbId);
        sendMemo.setMeMemo(content);
        sendMemo.setMeType("send");
        sendMemo.setMeSendId(sendId);
        memoMapper.insert(sendMemo);

        log.info("쪽지 발송: from={}, to={}", fromMbId, toMbId);
        return recvMemo;
    }

    /**
     * 받은 쪽지 목록
     */
    public List<Memo> getReceivedMemos(String mbId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<Memo> memos = memoMapper.findReceivedMemos(mbId, limit, offset);

        // 읽음 여부 설정
        for (Memo memo : memos) {
            memo.setIsRead(memo.getMeReadDatetime() != null &&
                          !memo.getMeReadDatetime().isEmpty() &&
                          !memo.getMeReadDatetime().startsWith("0"));
        }

        return memos;
    }

    /**
     * 보낸 쪽지 목록
     */
    public List<Memo> getSentMemos(String mbId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<Memo> memos = memoMapper.findSentMemos(mbId, limit, offset);

        // 읽음 여부 설정
        for (Memo memo : memos) {
            memo.setIsRead(memo.getMeReadDatetime() != null &&
                          !memo.getMeReadDatetime().isEmpty() &&
                          !memo.getMeReadDatetime().startsWith("0"));
        }

        return memos;
    }

    /**
     * 받은 쪽지 개수
     */
    public int getReceivedMemoCount(String mbId) {
        return memoMapper.countReceivedMemos(mbId);
    }

    /**
     * 보낸 쪽지 개수
     */
    public int getSentMemoCount(String mbId) {
        return memoMapper.countSentMemos(mbId);
    }

    /**
     * 읽지 않은 쪽지 개수
     */
    public int getUnreadMemoCount(String mbId) {
        return memoMapper.countUnreadMemos(mbId);
    }

    /**
     * 쪽지 상세 조회
     */
    public Memo getMemo(Integer meId, String mbId) {
        Memo memo = memoMapper.findById(meId);
        if (memo == null) {
            return null;
        }

        // 권한 확인
        if (!memo.getMeRecvMbId().equals(mbId) && !memo.getMeSendMbId().equals(mbId)) {
            throw new IllegalArgumentException("쪽지 열람 권한이 없습니다.");
        }

        // 읽음 여부 설정
        memo.setIsRead(memo.getMeReadDatetime() != null &&
                      !memo.getMeReadDatetime().isEmpty() &&
                      !memo.getMeReadDatetime().startsWith("0"));

        return memo;
    }

    /**
     * 쪽지 읽음 처리
     */
    @Transactional
    public void markAsRead(Integer meId, String mbId) {
        memoMapper.markAsRead(meId, mbId);
    }

    /**
     * 쪽지 삭제
     */
    @Transactional
    public void deleteMemo(Integer meId, String mbId) {
        memoMapper.delete(meId, mbId);
        log.info("쪽지 삭제: meId={}, mbId={}", meId, mbId);
    }

    /**
     * 쪽지 일괄 삭제
     */
    @Transactional
    public void deleteMemos(List<Integer> meIds, String mbId) {
        if (meIds == null || meIds.isEmpty()) {
            return;
        }
        memoMapper.deleteBatch(meIds, mbId);
        log.info("쪽지 일괄 삭제: count={}, mbId={}", meIds.size(), mbId);
    }
}
