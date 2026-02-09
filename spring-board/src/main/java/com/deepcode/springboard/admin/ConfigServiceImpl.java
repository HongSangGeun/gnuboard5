package com.deepcode.springboard.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigMapper configMapper;

    @Override
    public Config getConfig() {
        return configMapper.selectConfig();
    }

    @Override
    @Transactional
    public void updateConfig(Config config) {
        configMapper.updateConfig(config);
    }

    @Override
    public List<ThemeInfo> getThemes() {
        List<ThemeInfo> themes = new ArrayList<>();
        File themeDir = new File(System.getProperty("user.dir"), "../theme");

        Config config = getConfig();
        String currentTheme = config.getCfTheme();

        if (themeDir.exists() && themeDir.isDirectory()) {
            File[] files = themeDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        File index = new File(file, "index.php");
                        File head = new File(file, "head.php");
                        File tail = new File(file, "tail.php");

                        // Simple check for valid theme structure
                        if (index.exists() || head.exists() || tail.exists()) {
                            ThemeInfo info = getThemeInfo(file);
                            if (info.getThemeName().equals(currentTheme)) {
                                info.setActive(true);
                            }
                            themes.add(info);
                        }
                    }
                }
            }
        }
        return themes;
    }

    private ThemeInfo getThemeInfo(File dir) {
        ThemeInfo info = new ThemeInfo();
        info.setDir(dir.getName());
        info.setThemeName(dir.getName());

        File readme = new File(dir, "readme.txt");
        if (readme.exists()) {
            try {
                List<String> lines = Files.readAllLines(readme.toPath());
                for (String line : lines) {
                    String lower = line.toLowerCase();
                    if (lower.startsWith("theme name:"))
                        info.setThemeName(getValue(line));
                    if (lower.startsWith("version:"))
                        info.setVersion(getValue(line));
                    if (lower.startsWith("maker:"))
                        info.setMaker(getValue(line));
                    if (lower.startsWith("maker uri:"))
                        info.setMakerUri(getValue(line));
                    if (lower.startsWith("license:"))
                        info.setLicense(getValue(line));
                    if (lower.startsWith("license uri:"))
                        info.setLicenseUri(getValue(line));
                    if (lower.startsWith("detail:"))
                        info.setDetail(getValue(line));
                }
            } catch (IOException e) {
                // Ignore
            }
        }

        File screenshot = new File(dir, "screenshot.png");
        if (screenshot.exists()) {
            // Assuming static mapping creates /theme/ directory access or similar
            // Ideally we should use a utility to get the web path
            info.setScreenshot("/theme/" + dir.getName() + "/screenshot.png");
        }

        return info;
    }

    private String getValue(String line) {
        String[] parts = line.split(":", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

    @Override
    @Transactional
    public void updateTheme(String themeName) {
        Config config = getConfig();
        config.setCfTheme(themeName);
        configMapper.updateConfig(config);
    }
}
