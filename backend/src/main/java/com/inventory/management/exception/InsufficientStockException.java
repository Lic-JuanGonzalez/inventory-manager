package com.inventory.management.exception;

import java.math.BigDecimal;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productSku, String branchName,
                                      BigDecimal requested, BigDecimal available) {
        super("Insufficient stock for '%s' in '%s'. Requested: %s, Available: %s"
                .formatted(productSku, branchName, requested, available));
    }
}
