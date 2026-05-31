package com.inventory.management.dto.response;

import java.time.Instant;

public record BranchResponse(
        Long id,
        String name,
        String address,
        String phone,
        String email,
        Boolean active,
        Instant createdAt
) {}
