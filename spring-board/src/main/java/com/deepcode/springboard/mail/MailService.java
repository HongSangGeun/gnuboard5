package com.deepcode.springboard.mail;

import com.deepcode.springboard.member.Member;
import com.deepcode.springboard.member.MemberMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MailMapper mailMapper;
    private final MemberMapper memberMapper;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:시스템관리자}")
    private String fromName;

    /**
     * 단일 메일 발송
     */
    public boolean sendMail(String to, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);
            log.info("메일 발송 성공: to={}, subject={}", to, subject);
            return true;

        } catch (Exception e) {
            log.error("메일 발송 실패: to={}, error={}", to, e.getMessage());
            return false;
        }
    }

    /**
     * 첨부 파일 포함 메일 발송
     */
    public boolean sendMailWithAttachment(String to, String subject, String content,
                                         boolean isHtml, List<File> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            // 첨부 파일 추가
            if (attachments != null && !attachments.isEmpty()) {
                for (File file : attachments) {
                    helper.addAttachment(file.getName(), file);
                }
            }

            mailSender.send(message);
            log.info("첨부파일 포함 메일 발송 성공: to={}, attachments={}", to, attachments.size());
            return true;

        } catch (Exception e) {
            log.error("첨부파일 포함 메일 발송 실패: to={}, error={}", to, e.getMessage());
            return false;
        }
    }

    /**
     * CC, BCC 포함 메일 발송
     */
    public boolean sendMailWithCcBcc(String to, String subject, String content,
                                     boolean isHtml, String cc, String bcc) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc);
            }
            if (bcc != null && !bcc.isEmpty()) {
                helper.setBcc(bcc);
            }

            mailSender.send(message);
            log.info("CC/BCC 포함 메일 발송 성공: to={}, cc={}, bcc={}", to, cc, bcc);
            return true;

        } catch (Exception e) {
            log.error("CC/BCC 포함 메일 발송 실패: to={}, error={}", to, e.getMessage());
            return false;
        }
    }

    /**
     * 회원에게 개인화된 메일 발송 (템플릿 변수 치환)
     * {이름}, {닉네임}, {회원아이디}, {이메일} 치환
     */
    public boolean sendMailToMember(String mbId, String subject, String content, boolean isHtml) {
        Member member = memberMapper.findById(mbId);
        if (member == null || member.getMbEmail() == null || member.getMbEmail().isEmpty()) {
            log.warn("회원 정보가 없거나 이메일이 없음: mbId={}", mbId);
            return false;
        }

        // 템플릿 변수 치환
        String personalizedSubject = replaceTemplateVars(subject, member);
        String personalizedContent = replaceTemplateVars(content, member);

        return sendMail(member.getMbEmail(), personalizedSubject, personalizedContent, isHtml);
    }

    /**
     * Member 객체를 직접 받아서 메일 발송
     */
    public boolean sendMemberMail(Member member, String subject, String content) {
        if (member == null || member.getMbEmail() == null || member.getMbEmail().isEmpty()) {
            log.warn("회원 정보가 없거나 이메일이 없음");
            return false;
        }

        // 템플릿 변수 치환
        String personalizedSubject = replaceTemplateVars(subject, member);
        String personalizedContent = replaceTemplateVars(content, member);

        return sendMail(member.getMbEmail(), personalizedSubject, personalizedContent, true);
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
            .replace("{이메일}", member.getMbEmail() != null ? member.getMbEmail() : "");
    }

    /**
     * 메일 템플릿 저장
     */
    @Transactional
    public void saveMail(Mail mail) {
        mail.setMaTime(LocalDateTime.now());
        if (mail.getMaId() == null) {
            mailMapper.insert(mail);
        } else {
            mailMapper.update(mail);
        }
    }

    /**
     * 메일 템플릿 조회
     */
    public Mail getMail(Integer maId) {
        return mailMapper.findById(maId);
    }

    /**
     * 모든 메일 템플릿 조회
     */
    public List<Mail> getAllMails() {
        return mailMapper.findAll();
    }

    /**
     * 메일 템플릿 삭제
     */
    @Transactional
    public void deleteMail(Integer maId) {
        mailMapper.delete(maId);
    }

    /**
     * 테스트 메일 발송
     */
    public boolean sendTestMail(String to) {
        String subject = "[메일검사] 제목";
        String content = "<div style='font-size:9pt;'>" +
                        "[메일검사] 내용<br>" +
                        "이 내용이 제대로 보인다면 보내는 메일 서버에는 이상이 없는 것입니다.<br>" +
                        LocalDateTime.now() + "<br>" +
                        "이 메일 주소로는 회신되지 않습니다." +
                        "</div>";

        return sendMail(to, subject, content, true);
    }
}
