package com.deepcode.springboard.admin;

import com.deepcode.springboard.bbs.domain.Board;
import com.deepcode.springboard.bbs.mapper.BoardMapper;
import com.deepcode.springboard.board.BoardGroupService;
import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.config.G5ConfigService;
import com.deepcode.springboard.member.LoginUser;
import com.deepcode.springboard.member.SearchCriteria;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/mgmt/boards", "/mgmt/board"})
public class AdminBoardController {
    private final BoardMapper boardMapper;
    private final AdminBoardService adminBoardService;
    private final G5ConfigService configService;
    private final AdminSkinOptionService skinOptionService;
    private final BoardGroupService boardGroupService;

    public AdminBoardController(BoardMapper boardMapper,
                                AdminBoardService adminBoardService,
                                G5ConfigService configService,
                                AdminSkinOptionService skinOptionService,
                                BoardGroupService boardGroupService) {
        this.boardMapper = boardMapper;
        this.adminBoardService = adminBoardService;
        this.configService = configService;
        this.skinOptionService = skinOptionService;
        this.boardGroupService = boardGroupService;
    }

    @GetMapping
    public String list(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        model.addAttribute("boards", boardMapper.findAll());
        model.addAttribute("subMenu", "300100");
        model.addAttribute("pageTitle", "게시판관리");
        return "admin/board/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) String grId,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        model.addAttribute("mode", "create");
        BoardForm form = new BoardForm();
        if (grId != null && !grId.isBlank()) {
            form.setGrId(grId.trim());
        }
        model.addAttribute("form", form);
        addBoardFormOptions(model);
        model.addAttribute("subMenu", "300100");
        model.addAttribute("pageTitle", "게시판 생성");
        return "admin/board/form";
    }

    @PostMapping
    public String create(@ModelAttribute("form") BoardForm form, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        try {
            adminBoardService.create(form);
            redirectAttributes.addFlashAttribute("message", "게시판이 생성되었습니다.");
            return "redirect:/mgmt/board";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/mgmt/board/new";
        }
    }

    @GetMapping("/{boTable}/edit")
    public String editForm(@PathVariable String boTable, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Board board = boardMapper.findByTable(boTable);
        if (board == null) {
            redirectAttributes.addFlashAttribute("error", "게시판을 찾을 수 없습니다.");
            return "redirect:/mgmt/board";
        }
        if (!requireBoardManagePermission(session, board, redirectAttributes)) {
            return "redirect:/";
        }
        model.addAttribute("mode", "edit");
        model.addAttribute("form", adminBoardService.toForm(board));
        addBoardFormOptions(model);
        model.addAttribute("subMenu", "300100");
        model.addAttribute("pageTitle", "게시판 수정");
        return "admin/board/form";
    }

    @PostMapping("/{boTable}/edit")
    public String edit(@PathVariable String boTable,
                       @ModelAttribute("form") BoardForm form,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        Board board = boardMapper.findByTable(boTable);
        if (board == null) {
            redirectAttributes.addFlashAttribute("error", "게시판을 찾을 수 없습니다.");
            return "redirect:/mgmt/board";
        }
        if (!requireBoardManagePermission(session, board, redirectAttributes)) {
            return "redirect:/";
        }
        try {
            form.setBoTable(boTable);
            adminBoardService.update(form);
            redirectAttributes.addFlashAttribute("message", "게시판이 수정되었습니다.");
            return "redirect:/mgmt/board";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/mgmt/board/" + boTable + "/edit";
        }
    }

    @PostMapping("/{boTable}/delete")
    public String delete(@PathVariable String boTable,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        try {
            adminBoardService.delete(boTable);
            redirectAttributes.addFlashAttribute("message", "게시판이 삭제되었습니다.");
            return "redirect:/mgmt/board";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/mgmt/board";
        }
    }

    @GetMapping("/{boTable}/copy")
    public String copyForm(@PathVariable String boTable, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        model.addAttribute("boTable", boTable);
        model.addAttribute("subMenu", "300100");
        model.addAttribute("pageTitle", "게시판 복제");
        return "admin/board/copy";
    }

    @PostMapping("/{boTable}/copy")
    public String copy(@PathVariable String boTable,
                       @org.springframework.web.bind.annotation.RequestParam("targetBoTable") String targetBoTable,
                       @org.springframework.web.bind.annotation.RequestParam(value = "targetSubject", required = false) String targetSubject,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        try {
            adminBoardService.copy(boTable, targetBoTable, targetSubject);
            redirectAttributes.addFlashAttribute("message", "게시판이 복제되었습니다.");
            return "redirect:/mgmt/board";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/mgmt/board/" + boTable + "/copy";
        }
    }

    @GetMapping("/{boTable}/move")
    public String moveForm(@PathVariable String boTable, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        model.addAttribute("boTable", boTable);
        model.addAttribute("subMenu", "300100");
        model.addAttribute("pageTitle", "게시판 이동");
        return "admin/board/move";
    }

    @PostMapping("/{boTable}/move")
    public String move(@PathVariable String boTable,
                       @org.springframework.web.bind.annotation.RequestParam("targetBoTable") String targetBoTable,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!requireAdmin(session, redirectAttributes)) {
            return "redirect:/login";
        }
        try {
            adminBoardService.move(boTable, targetBoTable);
            redirectAttributes.addFlashAttribute("message", "게시판이 이동되었습니다.");
            return "redirect:/mgmt/board";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/mgmt/board/" + boTable + "/move";
        }
    }

    private boolean requireAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser user = loginUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return false;
        }
        if (!configService.isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "관리자 권한이 없습니다.");
            return false;
        }
        return true;
    }

    private boolean requireBoardManagePermission(HttpSession session, Board board, RedirectAttributes redirectAttributes) {
        LoginUser user = loginUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return false;
        }
        if (configService.isAdmin(user)) {
            return true;
        }
        if (isBoardAdmin(board.getBoAdmin(), user.getId())) {
            return true;
        }
        redirectAttributes.addFlashAttribute("error", "게시판 관리 권한이 없습니다.");
        return false;
    }

    private boolean isBoardAdmin(String boAdmin, String userId) {
        String normalizedUserId = userId == null ? "" : userId.trim();
        if (boAdmin == null || boAdmin.isBlank() || normalizedUserId.isBlank()) {
            return false;
        }
        String[] admins = boAdmin.split("[,\\s]+");
        for (String admin : admins) {
            if (normalizedUserId.equalsIgnoreCase(admin.trim())) {
                return true;
            }
        }
        return false;
    }

    private LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (value instanceof LoginUser) {
            return (LoginUser) value;
        }
        return null;
    }

    private void addBoardFormOptions(Model model) {
        model.addAttribute("boardSkinOptions", skinOptionService.listBoardSkins());
        SearchCriteria criteria = new SearchCriteria();
        criteria.setPage(1);
        criteria.setLimit(1000);
        criteria.setSst("gr_id");
        criteria.setSod("asc");
        model.addAttribute("boardGroupOptions", boardGroupService.getBoardGroups(criteria));
    }
}
