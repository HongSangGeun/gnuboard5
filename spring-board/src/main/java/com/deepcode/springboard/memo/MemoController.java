package com.deepcode.springboard.memo;

import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.member.LoginUser;
import com.deepcode.springboard.member.Member;
import com.deepcode.springboard.member.MemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/bbs/memo")
@RequiredArgsConstructor
public class MemoController {

    private final MemoService memoService;
    private final MemberMapper memberMapper;

    private static final int PAGE_SIZE = 15;

    /**
     * 받은쪽지 / 보낸쪽지 목록
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "recv") String kind,
                       @RequestParam(defaultValue = "1") int page,
                       HttpSession session, HttpServletRequest request, Model model) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login";

        String mbId = loginUser.getId();
        List<Memo> memos;
        int totalCount;

        if ("send".equals(kind)) {
            memos = memoService.getSentMemos(mbId, page, PAGE_SIZE);
            totalCount = memoService.getSentMemoCount(mbId);
        } else {
            kind = "recv";
            memos = memoService.getReceivedMemos(mbId, page, PAGE_SIZE);
            totalCount = memoService.getReceivedMemoCount(mbId);
        }

        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        model.addAttribute("kind", kind);
        model.addAttribute("memos", memos);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageTitle", "recv".equals(kind) ? "받은쪽지" : "보낸쪽지");

        if (isMobile(request)) {
            return "mobile/bbs/memo/list";
        }
        return "bbs/memo/list";
    }

    /**
     * 쪽지 상세보기 + 읽음처리
     */
    @GetMapping("/view/{meId}")
    public String view(@PathVariable Integer meId,
                       @RequestParam(defaultValue = "recv") String kind,
                       HttpSession session, HttpServletRequest request, Model model) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login";

        Memo memo = memoService.getMemo(meId, loginUser.getId());
        if (memo == null) {
            return "redirect:/bbs/memo?kind=" + kind;
        }

        // 받은 쪽지이고 본인이 수신자이면 읽음 처리
        if ("recv".equals(memo.getMeType()) && memo.getMeRecvMbId().equals(loginUser.getId())) {
            memoService.markAsRead(meId, loginUser.getId());
            memo.setIsRead(true);
        }

        model.addAttribute("memo", memo);
        model.addAttribute("kind", kind);
        model.addAttribute("pageTitle", "쪽지 보기");

        if (isMobile(request)) {
            return "mobile/bbs/memo/view";
        }
        return "bbs/memo/view";
    }

    /**
     * 쪽지 쓰기 폼
     */
    @GetMapping("/form")
    public String form(@RequestParam(required = false, name = "me_recv_mb_id") String meRecvMbId,
                       HttpSession session, HttpServletRequest request, Model model) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login";

        model.addAttribute("meRecvMbId", meRecvMbId != null ? meRecvMbId : "");
        model.addAttribute("pageTitle", "쪽지 보내기");

        if (isMobile(request)) {
            return "mobile/bbs/memo/form";
        }
        return "bbs/memo/form";
    }

    /**
     * 쪽지 발송 처리 (+SMS 옵션)
     */
    @PostMapping("/form")
    public String send(@RequestParam("me_recv_mb_id") String meRecvMbId,
                       @RequestParam("me_memo") String meMemo,
                       @RequestParam(value = "sendSms", required = false) String sendSms,
                       HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login";

        if (meRecvMbId == null || meRecvMbId.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "받는 사람을 입력해주세요.");
            return "redirect:/bbs/memo/form";
        }
        if (meMemo == null || meMemo.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "쪽지 내용을 입력해주세요.");
            return "redirect:/bbs/memo/form";
        }

        try {
            boolean wantSms = "on".equals(sendSms) || "true".equals(sendSms) || "1".equals(sendSms);
            String[] recipients = meRecvMbId.split(",");
            int successCount = 0;
            List<String> smsErrors = new java.util.ArrayList<>();

            for (String to : recipients) {
                String toId = to.trim();
                if (toId.isEmpty()) continue;
                String smsResult = memoService.sendMemoWithSms(loginUser.getId(), toId, meMemo, wantSms);
                successCount++;
                if (smsResult != null) {
                    smsErrors.add(toId + ": " + smsResult);
                }
            }

            if (successCount == 0) {
                redirectAttributes.addFlashAttribute("error", "받는 사람을 입력해주세요.");
                return "redirect:/bbs/memo/form";
            }

            if (!smsErrors.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning",
                        successCount + "명에게 쪽지 발송 완료. SMS 실패: " + String.join(", ", smsErrors));
            } else {
                redirectAttributes.addFlashAttribute("message",
                        successCount + "명에게 " + (wantSms ? "쪽지와 SMS가" : "쪽지가") + " 발송되었습니다.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/bbs/memo/form?me_recv_mb_id=" + meRecvMbId;
        }

        return "redirect:/bbs/memo?kind=send";
    }

    /**
     * 쪽지 삭제 (단건/일괄)
     */
    @PostMapping("/delete")
    public String delete(@RequestParam(value = "meId", required = false) List<Integer> meIds,
                         @RequestParam(defaultValue = "recv") String kind,
                         HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login";

        if (meIds == null || meIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "삭제할 쪽지를 선택해주세요.");
            return "redirect:/bbs/memo?kind=" + kind;
        }

        memoService.deleteMemos(meIds, loginUser.getId());
        redirectAttributes.addFlashAttribute("message", meIds.size() + "건의 쪽지가 삭제되었습니다.");
        return "redirect:/bbs/memo?kind=" + kind;
    }

    /**
     * 읽지 않은 쪽지 개수 (AJAX)
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public Map<String, Integer> unreadCount(HttpSession session) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null) {
            return Map.of("count", 0);
        }
        int count = memoService.getUnreadMemoCount(loginUser.getId());
        return Map.of("count", count);
    }

    /**
     * 회원 검색 (자동완성)
     */
    @GetMapping("/search-members")
    @ResponseBody
    public List<Map<String, String>> searchMembers(@RequestParam String query, HttpSession session) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null || query == null || query.isBlank()) {
            return List.of();
        }
        List<Member> members = memberMapper.searchByIdOrNick(query + "%", 10);
        return members.stream()
                .map(m -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", m.getMbId());
                    map.put("nick", m.getMbNick() != null ? m.getMbNick() : "");
                    return map;
                })
                .collect(Collectors.toList());
    }

    private LoginUser getLoginUser(HttpSession session) {
        return (LoginUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
    }

    private boolean isMobile(HttpServletRequest request) {
        Object value = request.getAttribute("isMobileView");
        return Boolean.TRUE.equals(value);
    }
}
