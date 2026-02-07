package com.gnuboard.springboard.menu;

import java.util.ArrayList;
import java.util.List;

public class SiteMenu {
    private final String code;
    private final String name;
    private final String link;
    private final String target;
    private final List<SiteMenu> children = new ArrayList<>();

    public SiteMenu(String code, String name, String link, String target) {
        this.code = code;
        this.name = name;
        this.link = link;
        this.target = target;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getTarget() {
        return target;
    }

    public List<SiteMenu> getChildren() {
        return children;
    }
}
