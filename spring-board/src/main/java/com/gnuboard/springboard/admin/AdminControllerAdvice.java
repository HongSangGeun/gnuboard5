package com.gnuboard.springboard.admin;

import com.gnuboard.springboard.admin.service.AdminMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice(basePackages = "com.gnuboard.springboard.admin")
@RequiredArgsConstructor
public class AdminControllerAdvice {

    private final AdminMenuService adminMenuService;

    @ModelAttribute("adminMenus")
    public Map<String, List<AdminMenuService.AdminMenu>> getAdminMenus() {
        return adminMenuService.getAdminMenus();
    }
}
