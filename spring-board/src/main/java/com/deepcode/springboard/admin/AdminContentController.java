package com.deepcode.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mgmt/content")
@RequiredArgsConstructor
public class AdminContentController {
    private final ContentMapper contentMapper;

    @GetMapping
    public String index(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 20;
        int totalCount = 0;
        int totalPage = 0;
        if (page < 1) {
            page = 1;
        }
        int offset = (page - 1) * pageSize;

        try {
            totalCount = contentMapper.countAll();
            totalPage = (int) Math.ceil((double) totalCount / pageSize);
            model.addAttribute("contents", contentMapper.findPage(offset, pageSize));
        } catch (Exception e) {
            model.addAttribute("contents", java.util.List.of());
            model.addAttribute("error", "내용 테이블이 없거나 조회할 수 없습니다.");
        }
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("page", page);
        model.addAttribute("subMenu", "300600");
        model.addAttribute("pageTitle", "내용관리");
        return "admin/content/list";
    }
}
