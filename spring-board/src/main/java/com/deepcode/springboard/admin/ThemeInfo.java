package com.deepcode.springboard.admin;

import lombok.Data;

@Data
public class ThemeInfo {
    private String name;
    private String version;
    private String maker;
    private String makerUri;
    private String license;
    private String licenseUri;
    private String detail;
    private String screenshot;
    private String themeName;
    private String dir;
    private boolean isActive;
}
