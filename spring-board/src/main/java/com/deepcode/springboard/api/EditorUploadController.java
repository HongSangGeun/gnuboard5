package com.deepcode.springboard.api;

import com.deepcode.springboard.bbs.service.OcrService;
import com.deepcode.springboard.common.G5Properties;
import com.deepcode.springboard.common.SessionConst;
import com.deepcode.springboard.member.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class EditorUploadController {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private final Path editorRootDir;
    private final Path ckeditorDir;
    private final Path cheditorDir;
    private final OcrService ocrService;

    public EditorUploadController(G5Properties properties, @Autowired(required = false) OcrService ocrService) {
        this.ocrService = ocrService;
        Path base = Paths.get(properties.getDataPath());
        if (!base.isAbsolute()) {
            Path springBoardDir = findSpringBoardDir();
            if (springBoardDir != null) {
                base = springBoardDir.resolve(base).normalize();
            } else {
                Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
                base = cwd.resolve(base).normalize();
            }
        }
        this.editorRootDir = base.resolve("editor").normalize();
        this.ckeditorDir = editorRootDir.resolve("ckeditor4").normalize();
        this.cheditorDir = editorRootDir.resolve("cheditor5").normalize();
    }

    private Path findSpringBoardDir() {
        Path home = new ApplicationHome(EditorUploadController.class).getDir().toPath().toAbsolutePath().normalize();
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

    @PostMapping({"/plugin/editor/ckeditor4/imageUpload/upload", "/bbs/{boTable}/plugin/editor/ckeditor4/imageUpload/upload"})
    @ResponseBody
    public Map<String, Object> uploadImage(
            HttpServletRequest request,
            @RequestParam(value = "upload", required = false) MultipartFile upload,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        if (!isAuthenticated(request)) {
            return error("로그인 후 업로드할 수 있습니다.");
        }
        MultipartFile target = upload != null ? upload : file;
        if (target == null || target.isEmpty()) {
            return error("업로드할 파일이 없습니다.");
        }
        if (target.getSize() > MAX_IMAGE_BYTES) {
            return error("이미지 용량이 너무 큽니다.");
        }

        String original = target.getOriginalFilename();
        String safeOriginal = original == null ? "" : original.replaceAll("[\\r\\n]", "");
        String extension = "";
        int dot = safeOriginal.lastIndexOf('.');
        if (dot >= 0 && dot < safeOriginal.length() - 1) {
            extension = safeOriginal.substring(dot);
        }
        String ext = extension.isEmpty() ? "" : extension.substring(1).toLowerCase(Locale.ROOT);
        if (ext.isEmpty()) {
            String contentType = target.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                ext = contentType.substring("image/".length()).toLowerCase(Locale.ROOT);
                if (ext.equals("jpeg")) {
                    ext = "jpg";
                }
                extension = "." + ext;
            }
        }
        if (ext.isEmpty() || !ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            return error("허용되지 않은 이미지 형식입니다.");
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
        File savedFile = null;
        try {
            Files.createDirectories(ckeditorDir);
            savedFile = ckeditorDir.resolve(storedName).toFile();
            target.transferTo(savedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return error("이미지 저장에 실패했습니다.");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploaded", 1);
        result.put("fileName", safeOriginal);
        result.put("url", request.getContextPath() + "/uploads/editor/ckeditor4/" + storedName);

        // OCR 처리 및 텍스트 파일로 저장
        if (ocrService != null && ocrService.isOcrAvailable() && savedFile != null) {
            String ocrText = ocrService.extractText(savedFile);
            if (ocrText != null && !ocrText.trim().isEmpty()) {
                result.put("ocrText", ocrText);
                // OCR 텍스트를 .ocr.txt 파일로 저장 (검색용)
                try {
                    Path ocrFile = ckeditorDir.resolve(storedName + ".ocr.txt");
                    Files.writeString(ocrFile, ocrText);
                } catch (IOException e) {
                    // OCR 파일 저장 실패해도 이미지 업로드는 성공
                }
            }
        }

        return result;
    }

    @PostMapping({"/plugin/editor/cheditor5/imageUpload/upload.php", "/bbs/{boTable}/plugin/editor/cheditor5/imageUpload/upload.php"})
    @ResponseBody
    public Map<String, Object> uploadCheditorImage(
            HttpServletRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "origname", required = false) String originalName) {
        if (!isAuthenticated(request)) {
            return cheditorError();
        }
        if (file == null || file.isEmpty()) {
            return cheditorError();
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            return cheditorError();
        }

        String sourceName = StringUtils.hasText(originalName) ? originalName : file.getOriginalFilename();
        String safeOriginal = sourceName == null ? "" : sourceName.replaceAll("[\\r\\n]", "");
        String extension = extractExtension(file, safeOriginal);
        String ext = extension.isEmpty() ? "" : extension.substring(1).toLowerCase(Locale.ROOT);
        if (ext.isEmpty() || !ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            return cheditorError();
        }

        String storedName = makeCheditorFileName(extension);
        File savedFile = null;
        try {
            Files.createDirectories(cheditorDir);
            savedFile = cheditorDir.resolve(storedName).toFile();
            file.transferTo(savedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return cheditorError();
        }

        Map<String, Object> result = new HashMap<>();
        String fileUrl = request.getContextPath() + "/uploads/editor/cheditor5/" + storedName;
        result.put("fileUrl", fileUrl);
        result.put("fileName", storedName);
        result.put("filePath", storedName);
        result.put("fileSize", file.getSize());

        // OCR 처리 및 텍스트 파일로 저장
        if (ocrService != null && ocrService.isOcrAvailable() && savedFile != null) {
            String ocrText = ocrService.extractText(savedFile);
            if (ocrText != null && !ocrText.trim().isEmpty()) {
                result.put("ocrText", ocrText);
                // OCR 텍스트를 .ocr.txt 파일로 저장 (검색용)
                try {
                    Path ocrFile = cheditorDir.resolve(storedName + ".ocr.txt");
                    Files.writeString(ocrFile, ocrText);
                } catch (IOException e) {
                    // OCR 파일 저장 실패해도 이미지 업로드는 성공
                }
            }
        }

        return result;
    }

    @PostMapping({"/plugin/editor/cheditor5/imageUpload/delete.php", "/bbs/{boTable}/plugin/editor/cheditor5/imageUpload/delete.php"})
    @ResponseBody
    public String deleteCheditorImage(HttpServletRequest request,
                                      @RequestParam(value = "filesrc", required = false) String filesrc) {
        if (!isAuthenticated(request)) {
            return "false";
        }
        if (!StringUtils.hasText(filesrc)) {
            return "false";
        }
        String candidate = Paths.get(filesrc).getFileName().toString();
        if (!StringUtils.hasText(candidate) || candidate.contains("..")) {
            return "false";
        }
        Path target = cheditorDir.resolve(candidate).normalize();
        if (!target.startsWith(cheditorDir)) {
            return "false";
        }
        try {
            if (Files.exists(target)) {
                Files.delete(target);
            }
            Path thumb = cheditorDir.resolve("thumb_" + candidate).normalize();
            if (Files.exists(thumb) && thumb.startsWith(cheditorDir)) {
                Files.delete(thumb);
            }
            return "true";
        } catch (IOException e) {
            return "false";
        }
    }

    @GetMapping("/uploads/editor/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        return serveEditorImage(ckeditorDir, filename);
    }

    @GetMapping("/uploads/editor/{editor}/{filename}")
    public ResponseEntity<Resource> serveImageByEditor(@PathVariable String editor, @PathVariable String filename) {
        Path dir;
        if ("ckeditor4".equalsIgnoreCase(editor)) {
            dir = ckeditorDir;
        } else if ("cheditor5".equalsIgnoreCase(editor)) {
            dir = cheditorDir;
        } else {
            return ResponseEntity.notFound().build();
        }
        return serveEditorImage(dir, filename);
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("uploaded", 0);
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        result.put("error", error);
        return result;
    }

    private String extractExtension(MultipartFile target, String safeOriginal) {
        String extension = "";
        int dot = safeOriginal.lastIndexOf('.');
        if (dot >= 0 && dot < safeOriginal.length() - 1) {
            extension = safeOriginal.substring(dot);
        }
        String ext = extension.isEmpty() ? "" : extension.substring(1).toLowerCase(Locale.ROOT);
        if (ext.isEmpty()) {
            String contentType = target.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                ext = contentType.substring("image/".length()).toLowerCase(Locale.ROOT);
                if ("jpeg".equals(ext)) {
                    ext = "jpg";
                }
                extension = "." + ext;
            }
        }
        return extension;
    }

    private String makeCheditorFileName(String extension) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toLowerCase(Locale.ROOT);
        return timestamp + "_" + random + extension.toLowerCase(Locale.ROOT);
    }

    private Map<String, Object> cheditorError() {
        Map<String, Object> result = new HashMap<>();
        result.put("fileUrl", "");
        result.put("fileName", "-ERR");
        result.put("filePath", "-ERR");
        result.put("fileSize", 0);
        return result;
    }

    private ResponseEntity<Resource> serveEditorImage(Path dir, String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        Path file = dir.resolve(filename).normalize();
        if (!file.startsWith(dir)) {
            return ResponseEntity.badRequest().build();
        }
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            String detected = Files.probeContentType(file);
            if (detected != null) {
                mediaType = MediaType.parseMediaType(detected);
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .contentType(mediaType)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object value = session.getAttribute(SessionConst.LOGIN_MEMBER);
        return value instanceof LoginUser;
    }
}
