package com.inventory.management.dto.request;

import com.inventory.management.domain.enums.MovementReason;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record InventoryMovementRequest(
        @NotNull Long productId,
        @NotNull Long branchId,
        @NotNull MovementReason reason,
        @NotNull @DecimalMin("0.001") BigDecimal quantity,
        String observations
) {}
