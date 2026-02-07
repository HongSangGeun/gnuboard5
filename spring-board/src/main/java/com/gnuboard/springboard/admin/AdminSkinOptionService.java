package com.gnuboard.springboard.admin;

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
    public List<String> listBoardSkins() {
        return listSkinDirs("board");
    }

    public List<String> listLatestSkins() {
        return listSkinDirs("latest");
    }

    private List<String> listSkinDirs(String skinType) {
        Set<String> names = new TreeSet<>();
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
