package com.gnuboard.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mgmt")
@RequiredArgsConstructor
public class AdminMemberMiscController {

    @GetMapping("/visit")
    public String visit(Model model) {
        model.addAttribute("subMenu", "200800");
        model.addAttribute("pageTitle", "접속자집계");
        return "admin/placeholder";
    }

    @GetMapping("/visit/search")
    public String visitSearch(Model model) {
        model.addAttribute("subMenu", "200810");
        model.addAttribute("pageTitle", "접속자검색");
        return "admin/placeholder";
    }

    @GetMapping("/visit/delete")
    public String visitDelete(Model model) {
        model.addAttribute("subMenu", "200820");
        model.addAttribute("pageTitle", "접속자로그삭제");
        return "admin/placeholder";
    }

    @GetMapping("/point")
    public String point(Model model) {
        model.addAttribute("subMenu", "200200");
        model.addAttribute("pageTitle", "포인트관리");
        return "admin/placeholder";
    }

    @GetMapping("/poll")
    public String poll(Model model) {
        model.addAttribute("subMenu", "200900");
        model.addAttribute("pageTitle", "투표관리");
        return "admin/placeholder";
    }
}
