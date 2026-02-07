package com.gnuboard.springboard.admin;

import com.gnuboard.springboard.board.BoardGroup;
import com.gnuboard.springboard.board.BoardGroupMember;
import com.gnuboard.springboard.board.BoardGroupMemberService;
import com.gnuboard.springboard.member.Member;
import com.gnuboard.springboard.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mgmt/boardgroupmember")
@RequiredArgsConstructor
public class AdminBoardGroupMemberController {

    private final MemberService memberService;
    private final BoardGroupMemberService boardGroupMemberService;

    @GetMapping("/form")
    public String form(@RequestParam String mbId,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Member member = memberService.getMember(mbId);
        if (member == null) {
            redirectAttributes.addFlashAttribute("error", "존재하지 않는 회원입니다.");
            return "redirect:/mgmt/member";
        }

        List<BoardGroup> accessEnabledGroups = boardGroupMemberService.getAccessEnabledGroups();
        List<BoardGroupMember> assignedGroups = boardGroupMemberService.getMemberGroupAssignments(mbId);

        model.addAttribute("member", member);
        model.addAttribute("accessEnabledGroups", accessEnabledGroups);
        model.addAttribute("assignedGroups", assignedGroups);
        model.addAttribute("subMenu", "300200");
        model.addAttribute("pageTitle", "접근가능그룹");
        return "admin/boardgroupmember/form";
    }

    @PostMapping("/add")
    public String add(@RequestParam String mbId,
                      @RequestParam String grId,
                      RedirectAttributes redirectAttributes) {
        try {
            boardGroupMemberService.addMemberToGroup(mbId, grId);
            redirectAttributes.addFlashAttribute("message", "접근가능 그룹이 추가되었습니다.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/mgmt/boardgroupmember/form?mbId=" + mbId;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam String mbId,
                         @RequestParam(name = "chk[]", required = false) String[] selectedIds,
                         RedirectAttributes redirectAttributes) {
        List<Long> gmIds = boardGroupMemberService.parseIds(selectedIds);
        if (gmIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "삭제할 그룹을 하나 이상 선택하세요.");
            return "redirect:/mgmt/boardgroupmember/form?mbId=" + mbId;
        }

        int removed = boardGroupMemberService.removeMemberFromGroups(mbId, gmIds);
        redirectAttributes.addFlashAttribute("message", removed + "건 삭제되었습니다.");
        return "redirect:/mgmt/boardgroupmember/form?mbId=" + mbId;
    }
}

