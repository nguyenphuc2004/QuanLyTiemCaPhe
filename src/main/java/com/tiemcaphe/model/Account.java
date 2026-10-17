package com.tiemcaphe.model;

import java.time.LocalDateTime;

public record Account(long id, String username, String fullName, Role role, boolean active, LocalDateTime createdAt) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
