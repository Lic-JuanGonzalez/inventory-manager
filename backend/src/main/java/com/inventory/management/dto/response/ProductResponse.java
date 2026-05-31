package com.inventory.management.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        String unitOfMeasure,
        BigDecimal referencePrice,
        Boolean active,
        Instant createdAt
) {}
