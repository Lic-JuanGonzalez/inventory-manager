package com.inventory.management.dto.response;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long userId,
        String userEmail,
        String action,
        String entityName,
        String entityId,
        String oldValues,
        String newValues,
        String ipAddress,
        String status,
        String errorMessage,
        Instant createdAt
) {}
