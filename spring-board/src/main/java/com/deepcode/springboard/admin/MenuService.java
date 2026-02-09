package com.deepcode.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;

    public List<Menu> getMenuList() {
        return menuMapper.findAll();
    }

    public Menu getMenu(int meId) {
        return menuMapper.findById(meId);
    }

    @Transactional
    public void addMenu(Menu menu) {
        menuMapper.insert(menu);
    }

    @Transactional
    public void updateMenu(Menu menu) {
        menuMapper.update(menu);
    }

    @Transactional
    public void deleteMenu(int meId) {
        menuMapper.delete(meId);
    }

    @Transactional
    public void deleteMenuByCode(String code) {
        menuMapper.deleteByCodePrefix(code);
    }
}
