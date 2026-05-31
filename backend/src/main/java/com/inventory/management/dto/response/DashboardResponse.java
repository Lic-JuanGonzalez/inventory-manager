package com.inventory.management.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        long lowStockCount,
        long pendingTransfersCount,
        long monthlyMovementsCount,
        BigDecimal totalStockValue,
        List<InventoryResponse> lowStockItems,
        List<TransferResponse> pendingTransfers,
        List<TopProductResponse> topMovedProducts
) {
    public record TopProductResponse(Long productId, String productName, Long movementCount) {}
}
