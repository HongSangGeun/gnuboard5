package com.deepcode.springboard.menu;

import com.deepcode.springboard.admin.Menu;
import com.deepcode.springboard.admin.MenuService;
import com.deepcode.springboard.bbs.domain.Board;
import com.deepcode.springboard.bbs.service.BoardService;
import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.member.LoginUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class SiteMenuAdvice {
    private static final Pattern BOARD_LINK_PATTERN = Pattern.compile("(^|/)bbs/([^/?#]+)(/|$)");
    private final MenuService menuService;
    private final BoardService boardService;

    public SiteMenuAdvice(MenuService menuService, BoardService boardService) {
        this.menuService = menuService;
        this.boardService = boardService;
    }

    @ModelAttribute("siteMenus")
    public List<SiteMenu> siteMenus(HttpSession session) {
        List<Menu> menus;
        try {
            menus = menuService.getMenuList();
        } catch (Exception e) {
            return List.of();
        }

        LoginUser loginUser = loginUser(session);
        Map<String, Boolean> boardAccessCache = new HashMap<>();
        Map<String, SiteMenu> topMap = new LinkedHashMap<>();
        Map<String, Boolean> topAccessibleMap = new HashMap<>();
        List<Menu> filtered = menus.stream()
                .filter(m -> m.getMeUse() == 1)
                .sorted(Comparator.comparingInt(Menu::getMeOrder))
                .toList();

        for (Menu menu : filtered) {
            String code = menu.getMeCode();
            if (code == null || code.length() < 2) {
                continue;
            }
            String target = menu.getMeTarget() == null || menu.getMeTarget().isBlank() ? "_self" : "_" + menu.getMeTarget();
            if (code.length() == 2) {
                String normalizedLink = normalizeLink(menu.getMeLink());
                topMap.put(code, new SiteMenu(code, menu.getMeName(), normalizedLink, target));
                topAccessibleMap.put(code, canAccessMenuLink(normalizedLink, loginUser, boardAccessCache));
            }
        }

        for (Menu menu : filtered) {
            String code = menu.getMeCode();
            if (code == null || code.length() < 4) {
                continue;
            }
            String prefix = code.substring(0, 2);
            SiteMenu parent = topMap.get(prefix);
            if (parent == null) {
                continue;
            }
            String normalizedLink = normalizeLink(menu.getMeLink());
            if (!canAccessMenuLink(normalizedLink, loginUser, boardAccessCache)) {
                continue;
            }
            String target = menu.getMeTarget() == null || menu.getMeTarget().isBlank() ? "_self" : "_" + menu.getMeTarget();
            parent.getChildren().add(new SiteMenu(code, menu.getMeName(), normalizedLink, target));
        }

        List<SiteMenu> visible = new ArrayList<>();
        for (Map.Entry<String, SiteMenu> entry : topMap.entrySet()) {
            boolean selfAccessible = topAccessibleMap.getOrDefault(entry.getKey(), false);
            if (selfAccessible || !entry.getValue().getChildren().isEmpty()) {
                visible.add(entry.getValue());
            }
        }
        return visible;
    }

    private String normalizeLink(String link) {
        if (link == null || link.isBlank()) {
            return "#";
        }
        String value = link.trim();
        if (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("//")
                || value.startsWith("mailto:")
                || value.startsWith("tel:")
                || value.startsWith("javascript:")
                || value.startsWith("#")) {
            return value;
        }
        if (!value.startsWith("/")) {
            return "/" + value;
        }
        return value;
    }

    private LoginUser loginUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    private boolean canAccessMenuLink(String link, LoginUser loginUser, Map<String, Boolean> boardAccessCache) {
        if (link == null || link.isBlank() || "#".equals(link)) {
            return true;
        }
        Matcher matcher = BOARD_LINK_PATTERN.matcher(link);
        if (!matcher.find()) {
            return true;
        }
        String boTable = matcher.group(2);
        if (boTable == null || boTable.isBlank() || "search".equalsIgnoreCase(boTable)) {
            return true;
        }
        return boardAccessCache.computeIfAbsent(boTable, key -> {
            try {
                Board board = boardService.getBoardOrThrow(key);
                boardService.assertListable(board, loginUser);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
