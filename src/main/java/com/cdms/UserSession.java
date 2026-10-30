package com.cdms;

public record UserSession(int userId, String username, String role, Integer officerId) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
