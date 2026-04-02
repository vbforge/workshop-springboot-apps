package com.vbforge.wookie.entity;

public enum Roles {

    USER("ROLE_USER"),
    RESTRICTED_USER("ROLE_RESTRICTED_USER"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN");

    private final String roleName;

    Roles(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static Roles fromString(String role) {
        for (Roles r : Roles.values()) {
            if (r.name().equalsIgnoreCase(role) || r.getRoleName().equalsIgnoreCase(role)) {
                return r;
            }
        }
        return USER;
    }
}