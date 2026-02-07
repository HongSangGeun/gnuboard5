package com.gnuboard.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mgmt/sms")
@RequiredArgsConstructor
public class AdminSmsController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("subMenu", "900000");
        model.addAttribute("pageTitle", "SMS 관리");
        return "admin/placeholder";
    }

    @GetMapping("/config")
    public String config(Model model) {
        model.addAttribute("subMenu", "900100");
        model.addAttribute("pageTitle", "SMS 기본설정");
        return "admin/placeholder";
    }

    @GetMapping("/member_update")
    public String memberUpdate(Model model) {
        model.addAttribute("subMenu", "900200");
        model.addAttribute("pageTitle", "회원정보업데이트");
        return "admin/placeholder";
    }

    @GetMapping("/write")
    public String write(Model model) {
        model.addAttribute("subMenu", "900300");
        model.addAttribute("pageTitle", "문자 보내기");
        return "admin/placeholder";
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("subMenu", "900400");
        model.addAttribute("pageTitle", "전송내역-건별");
        return "admin/placeholder";
    }

    @GetMapping("/history_num")
    public String historyNum(Model model) {
        model.addAttribute("subMenu", "900410");
        model.addAttribute("pageTitle", "전송내역-번호별");
        return "admin/placeholder";
    }

    @GetMapping("/emoticon_group")
    public String emoticonGroup(Model model) {
        model.addAttribute("subMenu", "900500");
        model.addAttribute("pageTitle", "이모티콘 그룹");
        return "admin/placeholder";
    }

    @GetMapping("/emoticon")
    public String emoticon(Model model) {
        model.addAttribute("subMenu", "900600");
        model.addAttribute("pageTitle", "이모티콘 관리");
        return "admin/placeholder";
    }

    @GetMapping("/hp_group")
    public String hpGroup(Model model) {
        model.addAttribute("subMenu", "900700");
        model.addAttribute("pageTitle", "휴대폰번호 그룹");
        return "admin/placeholder";
    }

    @GetMapping("/hp")
    public String hp(Model model) {
        model.addAttribute("subMenu", "900800");
        model.addAttribute("pageTitle", "휴대폰번호 관리");
        return "admin/placeholder";
    }

    @GetMapping("/hp_file")
    public String hpFile(Model model) {
        model.addAttribute("subMenu", "900900");
        model.addAttribute("pageTitle", "휴대폰번호 파일");
        return "admin/placeholder";
    }
}
