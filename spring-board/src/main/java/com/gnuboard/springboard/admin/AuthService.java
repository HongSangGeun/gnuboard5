package com.gnuboard.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthMapper authMapper;

    public List<AuthMapper.AuthDTO> getAuthList() {
        return authMapper.findAll();
    }

    @Transactional
    public void addAuth(Auth auth) {
        // Check if exists
        Auth existing = authMapper.findById(auth.getMbId(), auth.getAuMenu());
        if (existing != null) {
            authMapper.update(auth);
        } else {
            authMapper.insert(auth);
        }
    }

    @Transactional
    public void deleteAuth(String mbId, String auMenu) {
        authMapper.delete(mbId, auMenu);
    }
}
