package com.inventory.management.dto.response;

import com.inventory.management.domain.enums.RoleType;
import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String lastName,
        String fullName,
        String email,
        RoleType role,
        Boolean active,
        Instant createdAt
) {}
