package com.example.webshop.enums;

public enum Role {
    ADMIN,
    CUSTOMER;

    /** Authority name used by Spring Security, e.g. ROLE_ADMIN. */
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
