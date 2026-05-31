package com.inventory.management.exception;

import java.math.BigDecimal;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productSku, String branchName,
                                      BigDecimal requested, BigDecimal available) {
        super("Stock insuficiente para '%s' en '%s'. Solicitado: %s, Disponible: %s"
                .formatted(productSku, branchName, requested, available));
    }
}
