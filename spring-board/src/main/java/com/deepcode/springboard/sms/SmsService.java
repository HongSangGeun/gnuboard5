package com.deepcode.springboard.sms;

import com.deepcode.springboard.member.Member;
import com.deepcode.springboard.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SMS 발송 서비스
 *
 * 실제 SMS 발송은 외부 SMS 서비스 API를 연동해야 합니다.
 * 예: 알리고(Aligo), NHN Cloud SMS, 카카오알림톡 등
 *
 * 현재는 기본 구조만 구현되어 있으며, 실제 API 연동은
 * 사용할 SMS 서비스에 맞춰 구현해야 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsMapper smsMapper;
    private final MemberMapper memberMapper;

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.sms.api-key:}")
    private String apiKey;

    @Value("${app.sms.api-secret:}")
    private String apiSecret;

    @Value("${app.sms.from-number:}")
    private String fromNumber;

    /**
     * 단일 SMS 발송
     *
     * @param to 받는 번호 (010-1234-5678 형식)
     * @param message 메시지 내용
     * @return 발송 성공 여부
     */
    public boolean sendSms(String to, String message) {
        if (!smsEnabled) {
            log.warn("SMS 발송이 비활성화되어 있습니다.");
            return false;
        }

        // 전화번호 검증
        if (!isValidPhoneNumber(to)) {
            log.error("유효하지 않은 전화번호: {}", to);
            return false;
        }

        // 메시지 길이 검증 (SMS: 80자, LMS: 2000자)
        if (message.length() > 80) {
            log.warn("메시지가 SMS 길이를 초과합니다. LMS로 발송됩니다.");
            return sendLms(to, message);
        }

        try {
            // TODO: 실제 SMS API 호출 구현 필요
            // 예시: 알리고 API, NHN Cloud SMS API 등
            log.info("SMS 발송: to={}, message={}", maskPhoneNumber(to), message);

            // 실제 구현 예시 (알리고):
            // HttpResponse response = sendAligoSms(to, message);
            // return response.isSuccessful();

            // 현재는 로그만 출력하고 true 반환
            return true;

        } catch (Exception e) {
            log.error("SMS 발송 실패: to={}, error={}", maskPhoneNumber(to), e.getMessage());
            return false;
        }
    }

    /**
     * LMS (장문 메시지) 발송
     */
    public boolean sendLms(String to, String message) {
        if (!smsEnabled) {
            log.warn("SMS 발송이 비활성화되어 있습니다.");
            return false;
        }

        if (!isValidPhoneNumber(to)) {
            log.error("유효하지 않은 전화번호: {}", to);
            return false;
        }

        try {
            // TODO: 실제 LMS API 호출 구현 필요
            log.info("LMS 발송: to={}, length={}", maskPhoneNumber(to), message.length());
            return true;

        } catch (Exception e) {
            log.error("LMS 발송 실패: to={}, error={}", maskPhoneNumber(to), e.getMessage());
            return false;
        }
    }

    /**
     * 회원에게 SMS 발송
     */
    public boolean sendSmsToMember(String mbId, String message) {
        Member member = memberMapper.findById(mbId);
        if (member == null || member.getMbHp() == null || member.getMbHp().isEmpty()) {
            log.warn("회원 정보가 없거나 휴대폰 번호가 없음: mbId={}", mbId);
            return false;
        }

        // 템플릿 변수 치환
        String personalizedMessage = replaceTemplateVars(message, member);
        return sendSms(member.getMbHp(), personalizedMessage);
    }

    /**
     * 여러 회원에게 SMS 발송
     */
    @Transactional
    public SmsHistory sendSmsBulk(List<String> phoneNumbers, String message) {
        SmsHistory history = new SmsHistory();
        history.setWrMessage(message);
        history.setWrTotal(phoneNumbers.size());
        history.setWrSuccess(0);
        history.setWrFailure(0);

        for (String phoneNumber : phoneNumbers) {
            boolean result = sendSms(phoneNumber, message);
            if (result) {
                history.setWrSuccess(history.getWrSuccess() + 1);
            } else {
                history.setWrFailure(history.getWrFailure() + 1);
            }
        }

        // 발송 이력 저장
        smsMapper.insertHistory(history);
        return history;
    }

    /**
     * 템플릿 변수 치환
     */
    private String replaceTemplateVars(String text, Member member) {
        if (text == null) return "";

        return text
            .replace("{이름}", member.getMbName() != null ? member.getMbName() : "")
            .replace("{닉네임}", member.getMbNick() != null ? member.getMbNick() : "")
            .replace("{회원아이디}", member.getMbId() != null ? member.getMbId() : "")
            .replace("{휴대폰}", member.getMbHp() != null ? member.getMbHp() : "");
    }

    /**
     * 전화번호 유효성 검증
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        // 하이픈 제거
        String cleaned = phoneNumber.replaceAll("-", "");

        // 010, 011, 016, 017, 018, 019로 시작하는 10~11자리 숫자
        return cleaned.matches("^01[016789]\\d{7,8}$");
    }

    /**
     * 전화번호 마스킹 (로그용)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "***-****-****";
        }

        String cleaned = phoneNumber.replaceAll("-", "");
        if (cleaned.length() == 11) {
            return cleaned.substring(0, 3) + "-****-" + cleaned.substring(7);
        } else if (cleaned.length() == 10) {
            return cleaned.substring(0, 3) + "-***-" + cleaned.substring(6);
        }
        return "***-****-****";
    }

    /**
     * SMS 설정 조회
     */
    public SmsConfig getConfig() {
        return smsMapper.getConfig();
    }

    /**
     * SMS 설정 업데이트
     */
    @Transactional
    public void updateConfig(SmsConfig config) {
        smsMapper.updateConfig(config);
    }

    /**
     * SMS 발송 이력 조회
     */
    public List<SmsHistory> getHistory(int page, int limit) {
        int offset = (page - 1) * limit;
        return smsMapper.getHistory(limit, offset);
    }

    /**
     * SMS 발송 이력 총 개수
     */
    public int getHistoryCount() {
        return smsMapper.getHistoryCount();
    }

    /**
     * 테스트 SMS 발송
     */
    public boolean sendTestSms(String to) {
        String message = "[SMS 테스트] 이 메시지가 정상적으로 수신되었다면 SMS 발송 기능이 정상 작동하는 것입니다.";
        return sendSms(to, message);
    }
}
