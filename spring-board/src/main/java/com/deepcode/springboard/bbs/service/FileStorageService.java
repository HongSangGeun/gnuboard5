package com.deepcode.springboard.bbs.service;

import com.deepcode.springboard.common.G5Properties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {
    private final G5Properties g5Properties;
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    private static final int SIGNATURE_SAMPLE_SIZE = 64;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf", "txt", "zip",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "hwp"
    );
    private static final Map<String, Set<String>> ALLOWED_MIME_TYPES = Map.ofEntries(
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("gif", Set.of("image/gif")),
            Map.entry("webp", Set.of("image/webp")),
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed")),
            Map.entry("doc", Set.of("application/msword")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint")),
            Map.entry("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
            Map.entry("hwp", Set.of("application/x-hwp", "application/haansofthwp", "application/octet-stream"))
    );

    public FileStorageService(G5Properties g5Properties) {
        this.g5Properties = g5Properties;
    }

    public StoredFile store(String boTable, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("파일 용량이 너무 큽니다.");
        }
        String originalName = file.getOriginalFilename();
        String safeOriginal = originalName == null ? "" : originalName.replaceAll("[\\r\\n]", "");
        String extension = "";
        int dot = safeOriginal.lastIndexOf('.');
        if (dot >= 0 && dot < safeOriginal.length() - 1) {
            extension = safeOriginal.substring(dot);
        }
        String ext = extension.isEmpty() ? "" : extension.substring(1).toLowerCase(Locale.ROOT);
        if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다.");
        }
        String contentType = normalizeContentType(file.getContentType());
        Set<String> allowedMimeTypes = ALLOWED_MIME_TYPES.getOrDefault(ext, Set.of());
        if (!allowedMimeTypes.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다.");
        }
        byte[] signature = readSignature(file);
        if (!isSignatureAllowed(ext, signature)) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다.");
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;

        Path dir = resolveBoardDir(boTable);
        Files.createDirectories(dir);
        Path target = dir.resolve(storedName);
        file.transferTo(target.toFile());

        return new StoredFile(storedName, safeOriginal, file.getSize());
    }

    public Path resolveBoardDir(String boTable) {
        Path base = Paths.get(g5Properties.getDataPath());
        if (!base.isAbsolute()) {
            Path springBoardDir = findSpringBoardDir();
            if (springBoardDir != null) {
                base = springBoardDir.resolve(base).normalize();
            } else {
                base = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize().resolve(base).normalize();
            }
        }
        return base.resolve(Paths.get("file", boTable)).normalize();
    }

    private Path findSpringBoardDir() {
        Path home = new ApplicationHome(FileStorageService.class).getDir().toPath().toAbsolutePath().normalize();
        Path cursor = home;
        for (int i = 0; i < 8 && cursor != null; i++) {
            if (Files.exists(cursor.resolve("build.gradle"))
                    && cursor.getFileName() != null
                    && "spring-board".equals(cursor.getFileName().toString())) {
                return cursor;
            }
            cursor = cursor.getParent();
        }
        return null;
    }

    public Path resolveFile(String boTable, String storedName) {
        return resolveBoardDir(boTable).resolve(storedName);
    }

    public void delete(String boTable, String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        Path path = resolveFile(boTable, storedName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    public void deleteBoardDir(String boTable) {
        Path dir = resolveBoardDir(boTable);
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
        } catch (IOException ignored) {
        }
    }

    public void moveBoardDir(String fromBoTable, String toBoTable) {
        Path from = resolveBoardDir(fromBoTable);
        Path to = resolveBoardDir(toBoTable);
        if (!Files.exists(from)) {
            return;
        }
        try {
            Files.createDirectories(to.getParent());
            Files.move(from, to);
        } catch (IOException ignored) {
        }
    }

    public void copyBoardDir(String fromBoTable, String toBoTable) {
        Path from = resolveBoardDir(fromBoTable);
        Path to = resolveBoardDir(toBoTable);
        if (!Files.exists(from)) {
            return;
        }
        try {
            Files.walk(from).forEach(path -> {
                try {
                    Path target = to.resolve(from.relativize(path));
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(path, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    public record StoredFile(String storedName, String originalName, long size) {}

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int semicolon = contentType.indexOf(';');
        String normalized = semicolon >= 0 ? contentType.substring(0, semicolon) : contentType;
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private byte[] readSignature(MultipartFile file) throws IOException {
        try (var in = file.getInputStream()) {
            return in.readNBytes(SIGNATURE_SAMPLE_SIZE);
        }
    }

    private boolean isSignatureAllowed(String ext, byte[] signature) {
        if (signature == null || signature.length == 0) {
            return false;
        }
        return switch (ext) {
            case "jpg", "jpeg" -> startsWith(signature, hex("FF D8 FF"));
            case "png" -> startsWith(signature, hex("89 50 4E 47 0D 0A 1A 0A"));
            case "gif" -> startsWith(signature, "GIF87a".getBytes()) || startsWith(signature, "GIF89a".getBytes());
            case "webp" -> isWebp(signature);
            case "pdf" -> startsWith(signature, "%PDF-".getBytes());
            case "zip", "docx", "xlsx", "pptx" -> isZip(signature);
            case "doc", "xls", "ppt" -> startsWith(signature, hex("D0 CF 11 E0 A1 B1 1A E1"));
            case "hwp" -> startsWith(signature, hex("D0 CF 11 E0 A1 B1 1A E1")) || isZip(signature);
            case "txt" -> !containsNullByte(signature);
            default -> false;
        };
    }

    private boolean startsWith(byte[] source, byte[] prefix) {
        if (source.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (source[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] signature) {
        if (signature.length < 12) {
            return false;
        }
        byte[] riff = "RIFF".getBytes();
        byte[] webp = "WEBP".getBytes();
        return startsWith(signature, riff)
                && Arrays.equals(Arrays.copyOfRange(signature, 8, 12), webp);
    }

    private boolean isZip(byte[] signature) {
        return startsWith(signature, hex("50 4B 03 04"))
                || startsWith(signature, hex("50 4B 05 06"))
                || startsWith(signature, hex("50 4B 07 08"));
    }

    private boolean containsNullByte(byte[] signature) {
        for (byte b : signature) {
            if (b == 0x00) {
                return true;
            }
        }
        return false;
    }

    private byte[] hex(String hex) {
        String[] parts = hex.trim().split("\\s+");
        byte[] bytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            bytes[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return bytes;
    }
}
