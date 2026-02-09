package com.deepcode.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime; // Import LocalDateTime
import java.util.List;

@Controller
@RequestMapping("/mgmt/config/newwin")
@RequiredArgsConstructor
public class AdminNewWinController {

    private final NewWinService newWinService;

    @GetMapping
    public String index(Model model) {
        List<NewWin> newWinList = newWinService.getNewWinList();
        model.addAttribute("newWinList", newWinList);
        model.addAttribute("subMenu", "100310");
        model.addAttribute("pageTitle", "팝업레이어관리");
        return "admin/config/newwin_list";
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false, defaultValue = "0") int nwId, Model model) {
        NewWin newWin = new NewWin();
        if (nwId > 0) {
            newWin = newWinService.getNewWin(nwId);
        } else {
            // Set defaults
            newWin.setNwDivision("both");
            newWin.setNwDevice("both");
            newWin.setNwLeft(10);
            newWin.setNwTop(10);
            newWin.setNwWidth(450);
            newWin.setNwHeight(500);
            newWin.setNwBeginTime(LocalDateTime.now());
            newWin.setNwEndTime(LocalDateTime.now().plusDays(7));
        }

        model.addAttribute("newWin", newWin);
        model.addAttribute("subMenu", "100310");
        model.addAttribute("pageTitle", "팝업레이어 " + (nwId > 0 ? "수정" : "추가"));
        return "admin/config/newwin_form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute NewWin newWin) {
        if (newWin.getNwId() > 0) {
            newWinService.updateNewWin(newWin);
        } else {
            newWinService.addNewWin(newWin);
        }
        return "redirect:/mgmt/config/newwin";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam int nwId) {
        newWinService.deleteNewWin(nwId);
        return "redirect:/mgmt/config/newwin";
    }
}
