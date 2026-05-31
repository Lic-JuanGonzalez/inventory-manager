package com.inventory.management.dto.response;

import com.inventory.management.domain.enums.RoleType;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Long userId,
        String email,
        String fullName,
        RoleType role
) {
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn,
                                  Long userId, String email, String fullName, RoleType role) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, userId, email, fullName, role);
    }
}
