package com.deepcode.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mgmt/faq")
@RequiredArgsConstructor
public class AdminFaqController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("subMenu", "300700");
        model.addAttribute("pageTitle", "FAQ관리");
        return "admin/placeholder";
    }
}
