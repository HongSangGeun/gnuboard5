package com.gnuboard.springboard.security;

import com.gnuboard.springboard.common.SessionConst;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfTokenManager {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private CsrfTokenManager() {
    }

    public static String ensureToken(HttpSession session) {
        Object value = session.getAttribute(SessionConst.CSRF_TOKEN);
        if (value instanceof String token && !token.isBlank()) {
            return token;
        }
        String token = generateToken();
        session.setAttribute(SessionConst.CSRF_TOKEN, token);
        return token;
    }

    public static boolean matches(String expected, String actual) {
        if (expected == null || expected.isBlank() || actual == null || actual.isBlank()) {
            return false;
        }
        byte[] left = expected.getBytes(StandardCharsets.UTF_8);
        byte[] right = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    private static String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
