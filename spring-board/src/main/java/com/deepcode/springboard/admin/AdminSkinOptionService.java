package com.deepcode.springboard.admin;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class AdminSkinOptionService {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public List<String> listBoardSkins() {
        return listSkinDirs("board");
    }

    public List<String> listLatestSkins() {
        return listSkinDirs("latest");
    }

    private List<String> listSkinDirs(String skinType) {
        Set<String> names = new TreeSet<>();

        // 1. Check filesystem (for development)
        for (Path baseDir : candidateBaseDirs()) {
            Path skinRoot = baseDir.resolve("skin").resolve(skinType);
            if (!Files.isDirectory(skinRoot)) {
                continue;
            }
            try (var stream = Files.list(skinRoot)) {
                stream.filter(Files::isDirectory)
                        .map(path -> path.getFileName() == null ? "" : path.getFileName().toString())
                        .filter(name -> !name.isBlank())
                        .forEach(names::add);
            } catch (IOException ignored) {
                // skip invalid path
            }
        }

        // 2. Check classpath resources (for production JAR)
        try {
            Resource[] resources = resolver.getResources("classpath:static/skin/" + skinType + "/**/");
            for (Resource resource : resources) {
                String uri = resource.getURI().toString();
                // Extract skin name from path like: jar:file:/app/app.jar!/BOOT-INF/classes!/static/skin/board/basic/
                int skinTypeIndex = uri.indexOf("/skin/" + skinType + "/");
                if (skinTypeIndex > 0) {
                    String afterSkinType = uri.substring(skinTypeIndex + ("/skin/" + skinType + "/").length());
                    int nextSlash = afterSkinType.indexOf('/');
                    if (nextSlash > 0) {
                        String skinName = afterSkinType.substring(0, nextSlash);
                        if (!skinName.isBlank()) {
                            names.add(skinName);
                        }
                    }
                }
            }
        } catch (IOException ignored) {
            // skip classpath scan errors
        }

        if (names.isEmpty()) {
            return Collections.singletonList("basic");
        }
        return new ArrayList<>(names);
    }

    private List<Path> candidateBaseDirs() {
        String userDir = System.getProperty("user.dir", "");
        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get(userDir, "src", "main", "resources", "static"));
        candidates.add(Paths.get(userDir, "spring-board", "src", "main", "resources", "static"));
        candidates.add(Paths.get(userDir, "build", "resources", "main", "static"));
        return candidates;
    }
}
