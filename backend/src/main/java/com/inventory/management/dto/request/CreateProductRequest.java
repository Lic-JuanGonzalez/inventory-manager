package com.inventory.management.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Size(max = 50) String sku,
        @NotBlank @Size(max = 200) String name,
        String description,
        Long categoryId,
        @NotBlank @Size(max = 30) String unitOfMeasure,
        @NotNull @DecimalMin("0.00") BigDecimal referencePrice
) {}
