package com.gnuboard.springboard.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;
    private final PasswordHasher passwordHasher = new PasswordHasher();

    public void register(String mbId, String mbPassword, String mbName, String mbNick, String mbEmail) {
        Member member = new Member();
        member.setMbId(mbId);
        member.setMbPassword(normalizePasswordForStore(mbPassword));
        member.setMbName(mbName);
        member.setMbNick(mbNick);
        member.setMbEmail(mbEmail);
        member.setMbLevel(1); // Default level
        member.setMbHp("");
        // 신규 가입 계정은 관리자 승인 전까지 로그인 차단
        member.setMbInterceptDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        applyInsertDefaults(member);

        memberMapper.insertMember(member);
    }

    public LoginUser authenticate(String mbId, String password) {
        Member member = memberMapper.findById(mbId);
        if (member != null && passwordHasher.matches(password, member.getMbPassword())) {
            if (!defaultString(member.getMbLeaveDate()).isBlank()) {
                throw new IllegalStateException("탈퇴 처리된 계정입니다.");
            }
            if (!defaultString(member.getMbInterceptDate()).isBlank()) {
                throw new IllegalStateException("관리자 승인 대기중인 계정입니다. 승인 후 로그인 가능합니다.");
            }
            return new LoginUser(member.getMbId(), member.getMbName(), member.getMbNick(), member.getMbLevel());
        }
        return null;
    }

    public boolean checkPassword(String mbId, String password) {
        Member member = memberMapper.findById(mbId);
        return member != null && passwordHasher.matches(password, member.getMbPassword());
    }

    public List<Member> getMemberList(SearchCriteria criteria) {
        if (criteria.getPage() < 1)
            criteria.setPage(1);
        if (criteria.getLimit() < 1)
            criteria.setLimit(10); // default limit
        return memberMapper.findAll(criteria);
    }

    public List<Member> getMembersForSelect() {
        return memberMapper.findAllForSelect();
    }

    public int getMemberCount(SearchCriteria criteria) {
        return memberMapper.count(criteria);
    }

    public int getLeaveCount(SearchCriteria criteria) {
        return memberMapper.countLeave(criteria);
    }

    public int getInterceptCount(SearchCriteria criteria) {
        return memberMapper.countIntercept(criteria);
    }

    public Member getMember(String mbId) {
        return memberMapper.findById(mbId);
    }

    @Transactional
    public void addMember(Member member) {
        if (member.getMbPassword() != null && !member.getMbPassword().isBlank()) {
            member.setMbPassword(normalizePasswordForStore(member.getMbPassword()));
        } else {
            member.setMbPassword("");
        }
        applyInsertDefaults(member);
        memberMapper.insertMember(member);
    }

    @Transactional
    public void updateMember(Member member) {
        Member existing = memberMapper.findById(member.getMbId());
        if (existing == null) {
            throw new IllegalArgumentException("Member not found: " + member.getMbId());
        }

        // Handle password update logic (only if provided)
        if (member.getMbPassword() != null && !member.getMbPassword().isEmpty()) {
            existing.setMbPassword(normalizePasswordForStore(member.getMbPassword()));
        }

        // Update fields
        existing.setMbName(member.getMbName());
        existing.setMbNick(member.getMbNick());
        existing.setMbNickDate(defaultString(member.getMbNickDate()).isBlank() ? existing.getMbNickDate() : member.getMbNickDate());
        existing.setMbEmail(member.getMbEmail());
        existing.setMbHomepage(member.getMbHomepage());
        existing.setMbLevel(member.getMbLevel());
        existing.setMbPoint(member.getMbPoint());
        existing.setMbSex(member.getMbSex());
        existing.setMbBirth(member.getMbBirth());
        existing.setMbHp(member.getMbHp());
        existing.setMbTel(member.getMbTel());
        existing.setMbCertify(member.getMbCertify());
        existing.setMbAdult(member.getMbAdult());
        existing.setMbDupinfo(member.getMbDupinfo());
        existing.setMbZip1(member.getMbZip1());
        existing.setMbZip2(member.getMbZip2());
        existing.setMbAddr1(member.getMbAddr1());
        existing.setMbSignature(member.getMbSignature());
        existing.setMbRecommend(member.getMbRecommend());
        existing.setMbMailling(member.getMbMailling());
        existing.setMbSms(member.getMbSms());
        existing.setMbOpen(member.getMbOpen());
        existing.setMbAddr2(member.getMbAddr2());
        existing.setMbAddr3(member.getMbAddr3());
        existing.setMbAddrJibeon(member.getMbAddrJibeon());
        existing.setMbProfile(member.getMbProfile());
        existing.setMbMemo(member.getMbMemo());
        existing.setMbInterceptDate(member.getMbInterceptDate());
        existing.setMbLeaveDate(member.getMbLeaveDate());
        existing.setMb1(member.getMb1());
        existing.setMb2(member.getMb2());
        existing.setMb3(member.getMb3());
        existing.setMb4(member.getMb4());
        existing.setMb5(member.getMb5());
        existing.setMb6(member.getMb6());
        existing.setMb7(member.getMb7());
        existing.setMb8(member.getMb8());
        existing.setMb9(member.getMb9());
        existing.setMb10(member.getMb10());

        applyUpdateDefaults(existing);
        memberMapper.updateMember(existing);
    }

    @Transactional
    public void updateMemberFromList(Member member) {
        member.setMbId(defaultString(member.getMbId()));
        member.setMbInterceptDate(defaultString(member.getMbInterceptDate()));
        if (member.getMbLevel() <= 0) {
            member.setMbLevel(1);
        }
        memberMapper.updateMemberFromList(member);
    }

    @Transactional
    public void deleteMember(String mbId) {
        memberMapper.deleteMember(mbId);
    }

    private String normalizePasswordForStore(String rawOrHashed) {
        if (rawOrHashed == null) {
            return "";
        }
        String value = rawOrHashed.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.contains(":")) {
            return value;
        }
        if (value.startsWith("*") && value.length() == 41) {
            return value;
        }
        return passwordHasher.createHash(value);
    }

    private void applyInsertDefaults(Member member) {
        String nowDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String nowDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        member.setMbId(defaultString(member.getMbId()));
        member.setMbPassword(defaultString(member.getMbPassword()));
        member.setMbName(defaultString(member.getMbName()));
        member.setMbNick(defaultString(member.getMbNick()).isBlank() ? member.getMbId() : member.getMbNick());
        member.setMbNickDate(defaultString(member.getMbNickDate()).isBlank() ? nowDate : member.getMbNickDate());
        member.setMbEmail(defaultString(member.getMbEmail()));
        member.setMbHomepage(defaultString(member.getMbHomepage()));
        member.setMbSex(defaultString(member.getMbSex()));
        member.setMbBirth(defaultString(member.getMbBirth()));
        member.setMbTel(defaultString(member.getMbTel()));
        member.setMbHp(defaultString(member.getMbHp()));
        member.setMbCertify(defaultString(member.getMbCertify()));
        member.setMbDupinfo(defaultString(member.getMbDupinfo()));
        member.setMbZip1(defaultString(member.getMbZip1()));
        member.setMbZip2(defaultString(member.getMbZip2()));
        member.setMbAddr1(defaultString(member.getMbAddr1()));
        member.setMbAddr2(defaultString(member.getMbAddr2()));
        member.setMbAddr3(defaultString(member.getMbAddr3()));
        member.setMbAddrJibeon(defaultString(member.getMbAddrJibeon()));
        member.setMbSignature(defaultString(member.getMbSignature()));
        member.setMbRecommend(defaultString(member.getMbRecommend()));
        member.setMbTodayLogin(defaultString(member.getMbTodayLogin()).isBlank() ? nowDateTime : member.getMbTodayLogin());
        member.setMbLoginIp(defaultString(member.getMbLoginIp()));
        member.setMbDatetime(defaultString(member.getMbDatetime()).isBlank() ? nowDateTime : member.getMbDatetime());
        member.setMbIp(defaultString(member.getMbIp()));
        member.setMbLeaveDate(defaultString(member.getMbLeaveDate()));
        member.setMbInterceptDate(defaultString(member.getMbInterceptDate()));
        member.setMbEmailCertify(defaultString(member.getMbEmailCertify()).isBlank() ? nowDateTime : member.getMbEmailCertify());
        member.setMbEmailCertify2(defaultString(member.getMbEmailCertify2()));
        member.setMbMemo(defaultString(member.getMbMemo()));
        member.setMbLostCertify(defaultString(member.getMbLostCertify()));
        member.setMbOpenDate(defaultString(member.getMbOpenDate()).isBlank() ? nowDate : member.getMbOpenDate());
        member.setMbProfile(defaultString(member.getMbProfile()));
        member.setMbMemoCall(defaultString(member.getMbMemoCall()));
        member.setMb1(defaultString(member.getMb1()));
        member.setMb2(defaultString(member.getMb2()));
        member.setMb3(defaultString(member.getMb3()));
        member.setMb4(defaultString(member.getMb4()));
        member.setMb5(defaultString(member.getMb5()));
        member.setMb6(defaultString(member.getMb6()));
        member.setMb7(defaultString(member.getMb7()));
        member.setMb8(defaultString(member.getMb8()));
        member.setMb9(defaultString(member.getMb9()));
        member.setMb10(defaultString(member.getMb10()));

        if (member.getMbLevel() <= 0) {
            member.setMbLevel(2);
        }
    }

    private void applyUpdateDefaults(Member member) {
        member.setMbName(defaultString(member.getMbName()));
        member.setMbNick(defaultString(member.getMbNick()));
        member.setMbNickDate(defaultString(member.getMbNickDate()));
        member.setMbEmail(defaultString(member.getMbEmail()));
        member.setMbHomepage(defaultString(member.getMbHomepage()));
        member.setMbSex(defaultString(member.getMbSex()));
        member.setMbBirth(defaultString(member.getMbBirth()));
        member.setMbTel(defaultString(member.getMbTel()));
        member.setMbHp(defaultString(member.getMbHp()));
        member.setMbCertify(defaultString(member.getMbCertify()));
        member.setMbDupinfo(defaultString(member.getMbDupinfo()));
        member.setMbZip1(defaultString(member.getMbZip1()));
        member.setMbZip2(defaultString(member.getMbZip2()));
        member.setMbAddr1(defaultString(member.getMbAddr1()));
        member.setMbAddr2(defaultString(member.getMbAddr2()));
        member.setMbAddr3(defaultString(member.getMbAddr3()));
        member.setMbAddrJibeon(defaultString(member.getMbAddrJibeon()));
        member.setMbSignature(defaultString(member.getMbSignature()));
        member.setMbRecommend(defaultString(member.getMbRecommend()));
        member.setMbLeaveDate(defaultString(member.getMbLeaveDate()));
        member.setMbInterceptDate(defaultString(member.getMbInterceptDate()));
        member.setMbProfile(defaultString(member.getMbProfile()));
        member.setMbMemo(defaultString(member.getMbMemo()));
        member.setMb1(defaultString(member.getMb1()));
        member.setMb2(defaultString(member.getMb2()));
        member.setMb3(defaultString(member.getMb3()));
        member.setMb4(defaultString(member.getMb4()));
        member.setMb5(defaultString(member.getMb5()));
        member.setMb6(defaultString(member.getMb6()));
        member.setMb7(defaultString(member.getMb7()));
        member.setMb8(defaultString(member.getMb8()));
        member.setMb9(defaultString(member.getMb9()));
        member.setMb10(defaultString(member.getMb10()));
        if (member.getMbLevel() <= 0) {
            member.setMbLevel(2);
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }
}
