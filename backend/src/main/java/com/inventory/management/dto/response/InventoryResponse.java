package com.inventory.management.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        String categoryName,
        Long branchId,
        String branchName,
        BigDecimal currentStock,
        BigDecimal minStock,
        BigDecimal maxStock,
        String unitOfMeasure,
        BigDecimal referencePrice,
        BigDecimal stockValue,
        boolean belowMinStock,
        Instant updatedAt
) {}
