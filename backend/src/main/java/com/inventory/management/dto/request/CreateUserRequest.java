package com.inventory.management.dto.request;

import com.inventory.management.domain.enums.RoleType;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                 message = "Debe contener al menos una mayúscula, una minúscula y un número")
        String password,
        @NotNull RoleType role
) {}
