package com.gnuboard.springboard.admin;

import com.gnuboard.springboard.common.SessionConst;
import com.gnuboard.springboard.config.G5ConfigService;
import com.gnuboard.springboard.member.LoginUser;
import com.gnuboard.springboard.member.Member;
import com.gnuboard.springboard.member.MemberService;
import com.gnuboard.springboard.member.SearchCriteria;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/mgmt/member")
@RequiredArgsConstructor
public class AdminMemberController {

    private static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "mb_id", "mb_nick", "mb_name", "mb_level", "mb_email",
            "mb_tel", "mb_hp", "mb_point", "mb_datetime", "mb_ip", "mb_recommend"
    );

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "mb_id", "mb_name", "mb_nick", "mb_level", "mb_point", "mb_email",
            "mb_tel", "mb_hp", "mb_datetime", "mb_today_login", "mb_intercept_date", "mb_leave_date"
    );

    private final MemberService memberService;
    private final G5ConfigService configService;

    @GetMapping
    public String index(
            @ModelAttribute SearchCriteria criteria,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }

        normalizeCriteria(criteria);

        List<Member> members = memberService.getMemberList(criteria);
        int totalCount = memberService.getMemberCount(criteria);
        int leaveCount = memberService.getLeaveCount(criteria);
        int interceptCount = memberService.getInterceptCount(criteria);

        model.addAttribute("members", members);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("leaveCount", leaveCount);
        model.addAttribute("interceptCount", interceptCount);
        model.addAttribute("crit", criteria);
        model.addAttribute("subMenu", "200100");
        model.addAttribute("pageTitle", "회원관리");
        model.addAttribute("totalPage", (int) Math.ceil((double) totalCount / criteria.getLimit()));
        model.addAttribute("todayYmd", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

        return "admin/member/member_list";
    }

    @PostMapping("/bulk")
    public String bulkUpdate(HttpServletRequest request,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }

        String[] selected = request.getParameterValues("chk[]");
        if (selected == null) {
            selected = request.getParameterValues("chk");
        }
        String action = request.getParameter("act_button");
        if (selected == null || selected.length == 0) {
            redirectAttributes.addFlashAttribute("error", "처리할 회원을 하나 이상 선택하세요.");
            return "redirect:" + buildListRedirectUrl(request);
        }

        int updated = 0;
        int deleted = 0;
        for (String idx : selected) {
            String mbId = request.getParameter("mb_id[" + idx + "]");
            if (mbId == null || mbId.isBlank()) {
                continue;
            }

            if ("선택삭제".equals(action)) {
                memberService.deleteMember(mbId);
                deleted++;
                continue;
            }

            Member target = new Member();
            target.setMbId(mbId);
            target.setMbLevel(parseIntOrDefault(request.getParameter("mb_level[" + idx + "]"), 2));
            target.setMbMailling(request.getParameter("mb_mailling[" + idx + "]") != null ? 1 : 0);
            target.setMbOpen(request.getParameter("mb_open[" + idx + "]") != null ? 1 : 0);
            target.setMbSms(request.getParameter("mb_sms[" + idx + "]") != null ? 1 : 0);
            target.setMbAdult(request.getParameter("mb_adult[" + idx + "]") != null ? 1 : 0);
            String interceptDate = request.getParameter("mb_intercept_date[" + idx + "]");
            target.setMbInterceptDate(interceptDate == null ? "" : interceptDate);
            memberService.updateMemberFromList(target);
            updated++;
        }

        if ("선택삭제".equals(action)) {
            redirectAttributes.addFlashAttribute("message", deleted + "건 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("message", updated + "건 수정되었습니다.");
        }
        return "redirect:" + buildListRedirectUrl(request);
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false) String mbId,
                       Model model,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }

        Member member = new Member();
        if (mbId != null && !mbId.isBlank()) {
            member = memberService.getMember(mbId);
            if (member == null) {
                redirectAttributes.addFlashAttribute("error", "존재하지 않는 회원입니다.");
                return "redirect:/mgmt/member";
            }
            model.addAttribute("pageTitle", "회원수정");
        } else {
            member.setMbLevel(2);
            member.setMbMailling(1);
            member.setMbOpen(1);
            member.setMbNickDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            model.addAttribute("pageTitle", "회원추가");
        }

        model.addAttribute("member", member);
        model.addAttribute("subMenu", "200100");
        return "admin/member/member_form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Member member,
                       @RequestParam(required = false) String w,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }

        try {
            if ("u".equals(w)) {
                memberService.updateMember(member);
                redirectAttributes.addFlashAttribute("message", "회원정보가 수정되었습니다.");
            } else {
                memberService.addMember(member);
                redirectAttributes.addFlashAttribute("message", "회원이 추가되었습니다.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "회원 저장 중 오류가 발생했습니다: " + ex.getMessage());
            if (member.getMbId() != null && !member.getMbId().isBlank()) {
                return "redirect:/mgmt/member/form?mbId=" + member.getMbId();
            }
            return "redirect:/mgmt/member/form";
        }
        return "redirect:/mgmt/member";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String mbId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        memberService.deleteMember(mbId);
        redirectAttributes.addFlashAttribute("message", "회원이 삭제되었습니다.");
        return "redirect:/mgmt/member";
    }

    private void normalizeCriteria(SearchCriteria criteria) {
        if (criteria.getPage() < 1) {
            criteria.setPage(1);
        }
        if (criteria.getLimit() < 1) {
            criteria.setLimit(15);
        }

        String sfl = criteria.getSfl();
        if (sfl == null || sfl.isBlank() || !ALLOWED_SEARCH_FIELDS.contains(sfl)) {
            criteria.setSfl("mb_id");
        }

        String sst = criteria.getSst();
        if (sst == null || sst.isBlank() || !ALLOWED_SORT_FIELDS.contains(sst)) {
            criteria.setSst("mb_datetime");
        }

        String sod = criteria.getSod();
        if (!"asc".equalsIgnoreCase(sod) && !"desc".equalsIgnoreCase(sod)) {
            criteria.setSod("desc");
        } else {
            criteria.setSod(sod.toLowerCase());
        }
    }

    private String buildListRedirectUrl(HttpServletRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/mgmt/member");
        addIfPresent(builder, "sst", request.getParameter("sst"));
        addIfPresent(builder, "sod", request.getParameter("sod"));
        addIfPresent(builder, "sfl", request.getParameter("sfl"));
        addIfPresent(builder, "stx", request.getParameter("stx"));
        addIfPresent(builder, "page", request.getParameter("page"));
        return builder.toUriString();
    }

    private void addIfPresent(UriComponentsBuilder builder, String key, String value) {
        if (value != null && !value.isBlank()) {
            builder.queryParam(key, value);
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean requireAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser user = loginUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return false;
        }
        if (!configService.isAdmin(user)) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        return true;
    }

    private LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}
