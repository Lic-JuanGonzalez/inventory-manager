package com.inventory.management.dto.response;

import com.inventory.management.domain.enums.TransferStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        Long id,
        Long originBranchId,
        String originBranchName,
        Long destinationBranchId,
        String destinationBranchName,
        Long productId,
        String productSku,
        String productName,
        BigDecimal quantity,
        TransferStatus status,
        Long requestedById,
        String requestedByName,
        Long approvedById,
        String approvedByName,
        String notes,
        Instant requestDate,
        Instant approvalDate,
        Instant shipDate,
        Instant receptionDate
) {}
