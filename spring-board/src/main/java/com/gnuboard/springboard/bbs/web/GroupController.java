package com.gnuboard.springboard.bbs.web;

import com.gnuboard.springboard.bbs.domain.Board;
import com.gnuboard.springboard.bbs.mapper.BoardMapper;
import com.gnuboard.springboard.bbs.service.BoardPermissionDeniedException;
import com.gnuboard.springboard.bbs.service.BoardService;
import com.gnuboard.springboard.common.SessionConst;
import com.gnuboard.springboard.member.LoginUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GroupController {
    private final BoardMapper boardMapper;
    private final BoardService boardService;

    @GetMapping("/group")
    public String group(@RequestParam(value = "grId", required = false) String grId,
                        @RequestParam(value = "gr_id", required = false) String legacyGrId,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        String targetGroupId = normalizeGroupId(grId, legacyGrId);
        if (targetGroupId.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "게시판그룹 ID가 없습니다.");
            return "redirect:/bbs";
        }

        LoginUser loginUser = loginUser(session);
        List<Board> boards = boardMapper.findByGroup(targetGroupId).stream()
                .filter(board -> boardService.isGroupAccessible(board, loginUser))
                .filter(board -> isListable(board, loginUser))
                .toList();
        if (boards.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "접근 가능한 게시판이 없습니다.");
            return "redirect:/bbs";
        }

        return "redirect:/bbs/" + boards.get(0).getBoTable() + "/list";
    }

    private String normalizeGroupId(String grId, String legacyGrId) {
        if (grId != null && !grId.trim().isEmpty()) {
            return grId.trim();
        }
        if (legacyGrId != null && !legacyGrId.trim().isEmpty()) {
            return legacyGrId.trim();
        }
        return "";
    }

    private boolean isListable(Board board, LoginUser loginUser) {
        try {
            boardService.assertListable(board, loginUser);
            return true;
        } catch (BoardPermissionDeniedException e) {
            return false;
        }
    }

    private LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}
