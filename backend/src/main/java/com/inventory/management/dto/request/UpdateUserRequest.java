package com.inventory.management.dto.request;

import com.inventory.management.domain.enums.RoleType;
import jakarta.validation.constraints.*;

public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotNull RoleType role,
        @NotNull Boolean active
) {}
