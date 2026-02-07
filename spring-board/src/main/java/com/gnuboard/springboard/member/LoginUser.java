package com.gnuboard.springboard.member;

public class LoginUser {
    private final String id;
    private final String name;
    private final String nick;
    private final int level;

    public LoginUser(String id, String name, String nick, int level) {
        this.id = id;
        this.name = name;
        this.nick = nick;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return nick;
    }

    public int getLevel() {
        return level;
    }
}
