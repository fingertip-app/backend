package com.janginharou.global.security;

import com.janginharou.domain.user.entity.UserRole;

public record AuthenticatedUser(
        Long id,
        String supabaseId,
        String email,
        UserRole role
) {
    public boolean hasRole(UserRole requiredRole) {
        return role == requiredRole;
    }
}
