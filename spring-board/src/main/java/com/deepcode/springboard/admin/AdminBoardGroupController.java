package com.deepcode.springboard.admin;

import com.deepcode.springboard.board.BoardGroup;
import com.deepcode.springboard.board.BoardGroupService;
import com.deepcode.springboard.member.MemberService;
import com.deepcode.springboard.member.SearchCriteria;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/mgmt/boardgroup")
@RequiredArgsConstructor
public class AdminBoardGroupController {

    private final BoardGroupService boardGroupService;
    private final MemberService memberService;

    @GetMapping
    public String list(@ModelAttribute SearchCriteria criteria, Model model) {
        if (criteria.getPage() < 1)
            criteria.setPage(1);
        if (criteria.getLimit() < 1)
            criteria.setLimit(15);

        List<BoardGroup> groups = boardGroupService.getBoardGroups(criteria);
        int totalCount = boardGroupService.getBoardGroupCount(criteria);

        model.addAttribute("groups", groups);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("crit", criteria);
        model.addAttribute("subMenu", "300200");
        model.addAttribute("pageTitle", "게시판그룹설정");

        int totalPage = (int) Math.ceil((double) totalCount / criteria.getLimit());
        model.addAttribute("totalPage", totalPage);

        return "admin/boardgroup/list";
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false) String grId, Model model, RedirectAttributes redirectAttributes) {
        BoardGroup group = new BoardGroup();
        if (grId != null && !grId.isEmpty()) {
            group = boardGroupService.getBoardGroup(grId);
            if (group == null) {
                redirectAttributes.addFlashAttribute("error", "게시판그룹을 찾을 수 없습니다.");
                return "redirect:/mgmt/boardgroup";
            }
            model.addAttribute("pageTitle", "게시판그룹 수정");
        } else {
            group.setGrDevice("both"); // default
            model.addAttribute("pageTitle", "게시판그룹 생성");
        }

        model.addAttribute("group", group);
        model.addAttribute("adminMemberOptions", memberService.getMembersForSelect());
        model.addAttribute("groupMemberCount", group.getGrId() == null ? 0 : boardGroupService.getGroupMemberCount(group.getGrId()));
        model.addAttribute("boardCount", group.getGrId() == null ? 0 : boardGroupService.getBoardCount(group.getGrId()));
        model.addAttribute("subMenu", "300200");

        return "admin/boardgroup/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute BoardGroup group,
                       @RequestParam(required = false) String w,
                       RedirectAttributes redirectAttributes) {
        boolean isUpdate = "u".equals(w);
        try {
            boardGroupService.saveBoardGroup(group, isUpdate);
            redirectAttributes.addFlashAttribute("message", "게시판그룹이 저장되었습니다.");
            return "redirect:/mgmt/boardgroup/form?grId=" + group.getGrId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            if (group.getGrId() != null && !group.getGrId().isBlank()) {
                return "redirect:/mgmt/boardgroup/form?grId=" + group.getGrId();
            }
            return "redirect:/mgmt/boardgroup/form";
        }
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String grId) {
        boardGroupService.deleteBoardGroup(grId);
        return "redirect:/mgmt/boardgroup";
    }
}
