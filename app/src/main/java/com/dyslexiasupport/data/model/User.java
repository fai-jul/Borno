package com.dyslexiasupport.data.model;

public class User {
    private final String name;
    private final String email;
    private final Role role;
    private final String avatar;
    private final boolean isGuest;

    public User(String name, String email, Role role, String avatar, boolean isGuest) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
        this.isGuest = isGuest;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isGuest() {
        return isGuest;
    }
}
