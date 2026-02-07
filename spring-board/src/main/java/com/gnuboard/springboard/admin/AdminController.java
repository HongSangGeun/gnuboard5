package com.gnuboard.springboard.admin;

import com.gnuboard.springboard.member.Member;
import com.gnuboard.springboard.member.MemberService;
import com.gnuboard.springboard.member.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/mgmt")
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;

    @GetMapping
    public String index(Model model) {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setSst("mb_datetime");
        criteria.setSod("desc");
        criteria.setPage(1);
        criteria.setLimit(5);
        criteria.setOffset(0);

        List<Member> newMembers = memberService.getMemberList(criteria);
        int memberCount = memberService.getMemberCount(criteria);

        model.addAttribute("newMembers", newMembers);
        model.addAttribute("memberCount", memberCount);
        model.addAttribute("subMenu", "100000"); // Default to Config menu highlighting if applicable, or generic

        return "admin/index";
    }
}
