package com.deepcode.springboard.member;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final String DEFAULT_ALGO = "sha256";
    private static final int DEFAULT_ITERATIONS = 12000;
    private static final int DEFAULT_SALT_BYTES = 24;
    private static final int DEFAULT_HASH_BYTES = 24;

    public boolean matches(String raw, String hash) {
        if (raw == null || hash == null || hash.isBlank()) {
            return false;
        }
        if (hash.contains(":")) {
            return matchesPbkdf2(raw, hash);
        }
        if (hash.startsWith("*") && hash.length() == 41) {
            return matchesMysqlPassword(raw, hash);
        }
        return false;
    }

    public String createHash(String raw) {
        if (raw == null) {
            return "";
        }
        byte[] saltBytes = new byte[DEFAULT_SALT_BYTES];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        byte[] derived = pbkdf2(raw, saltBytes, DEFAULT_ITERATIONS, DEFAULT_HASH_BYTES, mapAlgorithm(DEFAULT_ALGO));
        String hash = Base64.getEncoder().encodeToString(derived);
        return DEFAULT_ALGO + ":" + DEFAULT_ITERATIONS + ":" + salt + ":" + hash;
    }

    private boolean matchesPbkdf2(String raw, String hash) {
        String[] parts = hash.split(":");
        if (parts.length < 4) {
            return false;
        }
        String algo = parts[0];
        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        byte[] salt;
        byte[] expected;
        try {
            salt = Base64.getDecoder().decode(parts[2]);
            expected = Base64.getDecoder().decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        String jceAlgo = mapAlgorithm(algo);
        if (jceAlgo == null) {
            return false;
        }

        byte[] derived = pbkdf2(raw, salt, iterations, expected.length, jceAlgo);
        return constantTimeEquals(expected, derived);
    }

    private String mapAlgorithm(String algo) {
        String lower = algo.toLowerCase();
        if ("sha256".equals(lower)) {
            return "PBKDF2WithHmacSHA256";
        }
        if ("sha1".equals(lower)) {
            return "PBKDF2WithHmacSHA1";
        }
        return null;
    }

    private byte[] pbkdf2(String raw, byte[] salt, int iterations, int length, String algorithm) {
        try {
            PBEKeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, iterations, length * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private boolean matchesMysqlPassword(String raw, String hash) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] stage1 = sha1.digest(raw.getBytes(StandardCharsets.UTF_8));
            byte[] stage2 = sha1.digest(stage1);
            String expected = "*" + toHex(stage2).toUpperCase();
            return constantTimeEquals(expected.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return false;
        }
        int diff = a.length ^ b.length;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
