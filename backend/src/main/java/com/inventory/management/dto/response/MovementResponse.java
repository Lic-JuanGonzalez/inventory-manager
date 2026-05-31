package com.inventory.management.dto.response;

import com.inventory.management.domain.enums.MovementReason;
import com.inventory.management.domain.enums.MovementType;
import java.math.BigDecimal;
import java.time.Instant;

public record MovementResponse(
        Long id,
        MovementType type,
        MovementReason reason,
        BigDecimal quantity,
        BigDecimal stockBefore,
        BigDecimal stockAfter,
        Long productId,
        String productSku,
        String productName,
        Long branchId,
        String branchName,
        Long userId,
        String userFullName,
        Long transferRequestId,
        String observations,
        Instant createdAt
) {}
