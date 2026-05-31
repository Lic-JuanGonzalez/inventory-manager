package com.inventory.management.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record InitInventoryRequest(
        @NotNull Long productId,
        @NotNull Long branchId,
        @NotNull @DecimalMin("0") BigDecimal currentStock,
        @NotNull @DecimalMin("0") BigDecimal minStock,
        @NotNull @DecimalMin("0") BigDecimal maxStock
) {}
