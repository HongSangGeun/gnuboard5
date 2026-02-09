package com.deepcode.springboard.admin;

import com.deepcode.springboard.mail.Mail;
import com.deepcode.springboard.mail.MailService;
import com.deepcode.springboard.member.Member;
import com.deepcode.springboard.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/mgmt/mail")
@RequiredArgsConstructor
public class AdminMailController {

    private final MailService mailService;
    private final MemberService memberService;

    /**
     * 메일 목록
     */
    @GetMapping
    public String list(Model model) {
        List<Mail> mails = mailService.getAllMails();
        model.addAttribute("mails", mails);
        model.addAttribute("pageTitle", "메일 관리");
        model.addAttribute("subMenu", "200300");
        return "admin/mail/list";
    }

    /**
     * 메일 작성 폼
     */
    @GetMapping("/form")
    public String form(@RequestParam(required = false) Integer maId, Model model) {
        Mail mail = new Mail();
        String htmlTitle = "메일 입력";

        if (maId != null) {
            mail = mailService.getMail(maId);
            if (mail == null) {
                model.addAttribute("error", "등록된 자료가 없습니다.");
                return "redirect:/mgmt/mail";
            }
            htmlTitle = "메일 수정";
        }

        model.addAttribute("mail", mail);
        model.addAttribute("htmlTitle", htmlTitle);
        model.addAttribute("pageTitle", "메일 관리");
        model.addAttribute("subMenu", "200300");
        return "admin/mail/form";
    }

    /**
     * 메일 저장
     */
    @PostMapping("/save")
    public String save(@ModelAttribute Mail mail, RedirectAttributes redirectAttributes) {
        try {
            mailService.saveMail(mail);
            redirectAttributes.addFlashAttribute("message", "메일이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mgmt/mail";
    }

    /**
     * 메일 삭제
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Integer maId, RedirectAttributes redirectAttributes) {
        try {
            mailService.deleteMail(maId);
            redirectAttributes.addFlashAttribute("message", "메일이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mgmt/mail";
    }

    /**
     * 메일 발송 폼 (회원 선택)
     */
    @GetMapping("/select")
    public String selectForm(Model model) {
        List<Mail> mails = mailService.getAllMails();
        model.addAttribute("mails", mails);
        model.addAttribute("pageTitle", "메일 발송");
        model.addAttribute("subMenu", "200300");
        return "admin/mail/select";
    }

    /**
     * 메일 발송 실행
     */
    @PostMapping("/send")
    public String sendMail(
            @RequestParam Integer maId,
            @RequestParam(required = false) String[] mbIds,
            @RequestParam(required = false) String customEmails,
            RedirectAttributes redirectAttributes) {

        Mail mail = mailService.getMail(maId);
        if (mail == null) {
            redirectAttributes.addFlashAttribute("error", "메일 템플릿이 없습니다.");
            return "redirect:/mgmt/mail/select";
        }

        int successCount = 0;
        int failCount = 0;

        // 선택된 회원에게 발송
        if (mbIds != null && mbIds.length > 0) {
            for (String mbId : mbIds) {
                boolean result = mailService.sendMailToMember(mbId, mail.getMaSubject(), mail.getMaContent(), true);
                if (result) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
        }

        // 직접 입력된 이메일로 발송
        if (customEmails != null && !customEmails.isEmpty()) {
            String[] emails = customEmails.split(",");
            for (String email : emails) {
                email = email.trim();
                if (email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    boolean result = mailService.sendMail(email, mail.getMaSubject(), mail.getMaContent(), true);
                    if (result) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                }
            }
        }

        redirectAttributes.addFlashAttribute("message",
                "메일 발송 완료: 성공 " + successCount + "건, 실패 " + failCount + "건");
        return "redirect:/mgmt/mail";
    }

    /**
     * 테스트 메일 발송 폼
     */
    @GetMapping("/test")
    public String testForm(Model model) {
        model.addAttribute("pageTitle", "메일 테스트");
        model.addAttribute("subMenu", "100300");
        return "admin/mail/test";
    }

    /**
     * 테스트 메일 발송
     */
    @PostMapping("/test")
    public String sendTestMail(@RequestParam String email, Model model) {
        String[] emails = email.split(",");
        int successCount = 0;
        int failCount = 0;

        for (String e : emails) {
            e = e.trim();
            if (e.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                boolean result = mailService.sendTestMail(e);
                if (result) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
        }

        model.addAttribute("successCount", successCount);
        model.addAttribute("failCount", failCount);
        model.addAttribute("emails", Arrays.asList(emails));
        model.addAttribute("pageTitle", "메일 테스트");
        model.addAttribute("subMenu", "100300");
        return "admin/mail/test";
    }
}
