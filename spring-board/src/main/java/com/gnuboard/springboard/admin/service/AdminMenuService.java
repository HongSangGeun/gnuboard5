package com.gnuboard.springboard.admin.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminMenuService {

    public Map<String, List<AdminMenu>> getAdminMenus() {
        Map<String, List<AdminMenu>> menus = new LinkedHashMap<>();

        // Menu 100: Configuration
        List<AdminMenu> menu100 = new ArrayList<>();
        menu100.add(new AdminMenu("100000", "환경설정", "/mgmt/config"));
        menu100.add(new AdminMenu("100100", "기본환경설정", "/mgmt/config"));
        menu100.add(new AdminMenu("100200", "관리권한설정", "/mgmt/config/auth"));
        menu100.add(new AdminMenu("100280", "테마설정", "/mgmt/config/theme"));
        menu100.add(new AdminMenu("100290", "메뉴설정", "/mgmt/config/menu"));
        menu100.add(new AdminMenu("100300", "메일 테스트", "/mgmt/config/mailtest"));
        menu100.add(new AdminMenu("100310", "팝업레이어관리", "/mgmt/config/newwin"));
        menu100.add(new AdminMenu("100800", "세션파일 일괄삭제", "/mgmt/config/session"));
        menu100.add(new AdminMenu("100900", "캐시파일 일괄삭제", "/mgmt/config/cache"));
        menu100.add(new AdminMenu("100910", "캡챠파일 일괄삭제", "/mgmt/config/captcha"));
        menu100.add(new AdminMenu("100920", "썸네일파일 일괄삭제", "/mgmt/config/thumbnail"));
        menu100.add(new AdminMenu("100500", "phpinfo()", "/mgmt/config/phpinfo"));
        menu100.add(new AdminMenu("100510", "Browscap 업데이트", "/mgmt/config/browscap"));
        menu100.add(new AdminMenu("100520", "접속로그 변환", "/mgmt/config/visit_convert"));
        menu100.add(new AdminMenu("100410", "DB업그레이드", "/mgmt/config/dbupgrade"));
        menu100.add(new AdminMenu("100400", "부가서비스", "/mgmt/config/service"));
        menus.put("menu100", menu100);

        // Menu 200: Member Management
        List<AdminMenu> menu200 = new ArrayList<>();
        menu200.add(new AdminMenu("200000", "회원관리", "/mgmt/member"));
        menu200.add(new AdminMenu("200100", "회원관리", "/mgmt/member"));
        menu200.add(new AdminMenu("200300", "회원메일발송", "/mgmt/member/mail"));
        menu200.add(new AdminMenu("200800", "접속자집계", "/mgmt/visit"));
        menu200.add(new AdminMenu("200810", "접속자검색", "/mgmt/visit/search"));
        menu200.add(new AdminMenu("200820", "접속자로그삭제", "/mgmt/visit/delete"));
        menu200.add(new AdminMenu("200200", "포인트관리", "/mgmt/point"));
        menu200.add(new AdminMenu("200900", "투표관리", "/mgmt/poll"));
        menus.put("menu200", menu200);

        // Menu 300: Board Management
        List<AdminMenu> menu300 = new ArrayList<>();
        menu300.add(new AdminMenu("300000", "게시판관리", "/mgmt/board"));
        menu300.add(new AdminMenu("300100", "게시판관리", "/mgmt/board"));
        menu300.add(new AdminMenu("300200", "게시판그룹관리", "/mgmt/boardgroup"));
        menu300.add(new AdminMenu("300300", "인기검색어관리", "/mgmt/board/poplist"));
        menu300.add(new AdminMenu("300400", "인기검색어순위", "/mgmt/board/poprank"));
        menu300.add(new AdminMenu("300500", "1:1문의설정", "/mgmt/board/qa"));
        menu300.add(new AdminMenu("300600", "내용관리", "/mgmt/content"));
        menu300.add(new AdminMenu("300700", "FAQ관리", "/mgmt/faq"));
        menu300.add(new AdminMenu("300820", "글,댓글 현황", "/mgmt/board/write_count"));
        menus.put("menu300", menu300);

        // Menu 900: SMS
        List<AdminMenu> menu900 = new ArrayList<>();
        menu900.add(new AdminMenu("900000", "SMS 관리", "/mgmt/sms"));
        menu900.add(new AdminMenu("900100", "SMS 기본설정", "/mgmt/sms/config"));
        menu900.add(new AdminMenu("900200", "회원정보업데이트", "/mgmt/sms/member_update"));
        menu900.add(new AdminMenu("900300", "문자 보내기", "/mgmt/sms/write"));
        menu900.add(new AdminMenu("900400", "전송내역-건별", "/mgmt/sms/history"));
        menu900.add(new AdminMenu("900410", "전송내역-번호별", "/mgmt/sms/history_num"));
        menu900.add(new AdminMenu("900500", "이모티콘 그룹", "/mgmt/sms/emoticon_group"));
        menu900.add(new AdminMenu("900600", "이모티콘 관리", "/mgmt/sms/emoticon"));
        menu900.add(new AdminMenu("900700", "휴대폰번호 그룹", "/mgmt/sms/hp_group"));
        menu900.add(new AdminMenu("900800", "휴대폰번호 관리", "/mgmt/sms/hp"));
        menu900.add(new AdminMenu("900900", "휴대폰번호 파일", "/mgmt/sms/hp_file"));
        menus.put("menu900", menu900);

        return menus;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminMenu {
        private String code;
        private String name;
        private String url;
        // extended fields can be added
    }
}
