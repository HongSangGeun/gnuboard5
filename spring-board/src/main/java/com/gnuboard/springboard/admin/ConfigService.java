package com.gnuboard.springboard.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List; // Assuming List is from java.util

public interface ConfigService {
    Config getConfig();

    void updateConfig(Config config);

    List<ThemeInfo> getThemes();

    void updateTheme(String themeName);
}
