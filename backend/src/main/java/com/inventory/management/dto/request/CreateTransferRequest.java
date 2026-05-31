package com.inventory.management.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotNull Long originBranchId,
        @NotNull Long destinationBranchId,
        @NotNull Long productId,
        @NotNull @DecimalMin("0.001") BigDecimal quantity,
        String notes
) {}
