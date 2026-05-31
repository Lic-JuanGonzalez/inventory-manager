package com.inventory.management.dto.request;

import jakarta.validation.constraints.*;

public record CreateBranchRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 255) String address,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email
) {}
