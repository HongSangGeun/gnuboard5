package com.deepcode.springboard.bbs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DocumentConvertService {

    private static final long TIMEOUT_SECONDS = 30;

    /**
     * HWP 파일을 PDF로 변환한다.
     * 변환된 PDF는 원본과 같은 디렉토리에 {원본이름}.pdf 로 캐싱되며,
     * 캐시가 있으면 재변환 없이 바로 반환한다.
     */
    public Path convertToPdf(Path source) throws IOException {
        Path cacheFile = source.resolveSibling(stripExtension(source.getFileName().toString()) + ".pdf");
        if (Files.exists(cacheFile) && Files.size(cacheFile) > 0) {
            return cacheFile;
        }

        Path outDir = source.getParent();
        ProcessBuilder pb = new ProcessBuilder(
                "libreoffice", "--headless", "--convert-to", "pdf",
                "--outdir", outDir.toString(),
                source.toString()
        );
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("LibreOffice 변환 타임아웃 (" + TIMEOUT_SECONDS + "초 초과)");
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String output = new String(process.getInputStream().readAllBytes());
                throw new IOException("LibreOffice 변환 실패 (exit=" + exitCode + "): " + output);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("LibreOffice 변환 중 인터럽트 발생", e);
        }

        if (!Files.exists(cacheFile) || Files.size(cacheFile) == 0) {
            throw new IOException("PDF 변환 결과 파일이 생성되지 않았습니다: " + cacheFile);
        }

        log.info("HWP→PDF 변환 완료: {} → {}", source.getFileName(), cacheFile.getFileName());
        return cacheFile;
    }

    private String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
