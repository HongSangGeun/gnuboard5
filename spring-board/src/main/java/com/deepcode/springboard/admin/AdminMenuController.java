package com.deepcode.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/mgmt/config/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    @GetMapping
    public String index(Model model) {
        List<Menu> menus = menuService.getMenuList();
        model.addAttribute("menus", menus);
        model.addAttribute("subMenu", "100290");
        model.addAttribute("pageTitle", "메뉴설정");
        return "admin/config/menu_list";
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false, defaultValue = "0") int id,
            @RequestParam(required = false) String code,
            Model model) {
        Menu menu = new Menu();
        if (id > 0) {
            menu = menuService.getMenu(id);
        } else if (code != null) {
            // Suggest next code logic omitted for brevity, just pass parent code or max
            // code
            menu.setMeCode(code);
        }

        model.addAttribute("menu", menu);
        model.addAttribute("subMenu", "100290"); // Keep menu highlighted
        model.addAttribute("pageTitle", "메뉴 추가/수정");
        return "admin/config/menu_form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Menu menu) {
        if (menu.getMeId() > 0) {
            menuService.updateMenu(menu);
        } else {
            menuService.addMenu(menu);
        }
        return "redirect:/mgmt/config/menu";
    }

    @PostMapping("/update")
    public String updateAll(@RequestParam(required = false) List<Integer> meId,
            @RequestParam(required = false) List<String> meName,
            @RequestParam(required = false) List<String> meLink,
            @RequestParam(required = false) List<String> meTarget,
            @RequestParam(required = false) List<Integer> meOrder,
            @RequestParam(required = false) List<Integer> meUse,
            @RequestParam(required = false) List<Integer> meMobileUse) {
        if (meId == null || meId.isEmpty()) {
            return "redirect:/mgmt/config/menu";
        }

        for (int i = 0; i < meId.size(); i++) {
            Integer id = meId.get(i);
            if (id == null || id <= 0) {
                continue;
            }

            Menu current = menuService.getMenu(id);
            if (current == null) {
                continue;
            }

            current.setMeName(getAt(meName, i));
            current.setMeLink(getAt(meLink, i));
            current.setMeTarget(getAt(meTarget, i));
            current.setMeOrder(getAt(meOrder, i, current.getMeOrder()));
            current.setMeUse(getAt(meUse, i, current.getMeUse()));
            current.setMeMobileUse(getAt(meMobileUse, i, current.getMeMobileUse()));
            menuService.updateMenu(current);
        }

        return "redirect:/mgmt/config/menu";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam int meId) {
        menuService.deleteMenu(meId);
        return "redirect:/mgmt/config/menu";
    }

    private String getAt(List<String> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return "";
        }
        return values.get(index) == null ? "" : values.get(index).trim();
    }

    private int getAt(List<Integer> values, int index, int defaultValue) {
        if (values == null || index < 0 || index >= values.size() || values.get(index) == null) {
            return defaultValue;
        }
        return values.get(index);
    }
}
