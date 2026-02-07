package com.gnuboard.springboard.config;

import com.gnuboard.springboard.member.LoginUser;
import com.gnuboard.springboard.member.Member;
import com.gnuboard.springboard.member.MemberMapper;
import org.springframework.stereotype.Service;

@Service
public class G5ConfigService {
    private final G5ConfigMapper configMapper;
    private final MemberMapper memberMapper;

    public G5ConfigService(G5ConfigMapper configMapper, MemberMapper memberMapper) {
        this.configMapper = configMapper;
        this.memberMapper = memberMapper;
    }

    public String getAdminId() {
        try {
            G5Config config = configMapper.load();
            if (config == null) {
                return "";
            }
            return config.getCfAdmin() == null ? "" : config.getCfAdmin();
        } catch (Exception e) {
            // Table doesn't exist or other DB error -> treat as not installed
            return "";
        }
    }

    public boolean isAdmin(LoginUser user) {
        if (user == null) {
            return false;
        }
        String userId = normalize(user.getId());
        // GNUBoard 관례상 레벨 10 계정은 관리자 권한으로 취급
        if (user.getLevel() >= 10) {
            return true;
        }
        // 기존 운영 관례 호환: 기본 관리자 아이디는 권한 계층과 무관하게 슈퍼관리자로 처리
        if ("admin".equalsIgnoreCase(userId)) {
            return true;
        }
        String adminId = normalize(getAdminId());
        if (!adminId.isBlank() && adminId.equalsIgnoreCase(userId)) {
            return true;
        }

        // 세션의 레벨 정보가 오래된 경우를 대비해 DB 기준으로 최종 판정
        Member member = null;
        try {
            member = memberMapper.findById(userId);
        } catch (Exception ignored) {
            // DB 조회 실패 시 세션/설정 기반 판정만 사용
        }
        if (member == null) {
            return false;
        }
        if (member.getMbLevel() >= 10) {
            return true;
        }
        String memberId = normalize(member.getMbId());
        if ("admin".equalsIgnoreCase(memberId)) {
            return true;
        }
        return !adminId.isBlank() && adminId.equalsIgnoreCase(memberId);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
