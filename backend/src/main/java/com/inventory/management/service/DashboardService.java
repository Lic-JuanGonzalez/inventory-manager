package com.inventory.management.service;

import com.inventory.management.domain.enums.TransferStatus;
import com.inventory.management.domain.repository.InventoryMovementRepository;
import com.inventory.management.domain.repository.InventoryRepository;
import com.inventory.management.domain.repository.TransferRequestRepository;
import com.inventory.management.dto.response.DashboardResponse;
import com.inventory.management.dto.response.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final TransferRequestRepository transferRepository;
    private final InventoryService inventoryService;
    private final TransferService transferService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Instant monthStart = Instant.now().minus(30, ChronoUnit.DAYS);

        var lowStockItems    = inventoryRepository.findLowStockItems();
        var pendingTransfers = transferRepository.findByStatus(TransferStatus.PENDIENTE);
        long monthlyMovements = movementRepository.countByDateRange(monthStart, Instant.now());
        BigDecimal totalValue = inventoryRepository.calculateTotalValue(null);

        List<Object[]> topMovedRaw = movementRepository.findTopMovedProducts(
                monthStart, PageRequest.of(0, 5));

        List<DashboardResponse.TopProductResponse> topProducts = topMovedRaw.stream()
                .map(row -> new DashboardResponse.TopProductResponse(
                        (Long) row[0], (String) row[1], (Long) row[2]))
                .toList();

        List<TransferResponse> pendingResponses = pendingTransfers.stream()
                .map(t -> transferService.findById(t.getId()))
                .toList();

        return new DashboardResponse(
                lowStockItems.size(),
                pendingTransfers.size(),
                monthlyMovements,
                totalValue != null ? totalValue : BigDecimal.ZERO,
                lowStockItems.stream().map(inventoryService::toResponse).toList(),
                pendingResponses,
                topProducts
        );
    }
}
