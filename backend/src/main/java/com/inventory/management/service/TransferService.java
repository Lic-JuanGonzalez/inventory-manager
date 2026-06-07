package com.inventory.management.service;

import com.inventory.management.domain.entity.*;
import com.inventory.management.domain.enums.MovementReason;
import com.inventory.management.domain.enums.TransferStatus;
import com.inventory.management.domain.repository.TransferRequestRepository;
import com.inventory.management.domain.repository.TransferRequestSpecs;
import com.inventory.management.dto.request.CreateTransferRequest;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.dto.response.TransferResponse;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.exception.InsufficientStockException;
import com.inventory.management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRequestRepository transferRepository;
    private final ProductService productService;
    private final BranchService branchService;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<TransferResponse> findAll(TransferStatus status, Long originBranchId,
                                                  Long destinationBranchId, Long productId,
                                                  Pageable pageable) {
        return PageResponse.of(
                transferRepository.findAll(
                        TransferRequestSpecs.withFilters(status, originBranchId, destinationBranchId, productId),
                        pageable)
                        .map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> findPending() {
        return transferRepository.findByStatus(TransferStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TransferResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public TransferResponse create(CreateTransferRequest req, User requestedBy) {
        if (req.originBranchId().equals(req.destinationBranchId())) {
            throw new BusinessException("Origin and destination branches must be different");
        }
        Branch origin      = branchService.getOrThrow(req.originBranchId());
        Branch destination = branchService.getOrThrow(req.destinationBranchId());
        Product product    = productService.getOrThrow(req.productId());

        // verify origin has stock
        Inventory originInventory = inventoryService.getInventoryOrThrow(product.getId(), origin.getId());
        if (originInventory.getCurrentStock().compareTo(req.quantity()) < 0) {
            throw new InsufficientStockException(
                    product.getSku(), origin.getName(), req.quantity(), originInventory.getCurrentStock());
        }

        TransferRequest transfer = TransferRequest.builder()
                .originBranch(origin)
                .destinationBranch(destination)
                .product(product)
                .quantity(req.quantity())
                .requestedBy(requestedBy)
                .notes(req.notes())
                .status(TransferStatus.PENDING)
                .build();
        transfer = transferRepository.save(transfer);

        auditService.logAsync(requestedBy, "CREATE_TRANSFER", "TRANSFER",
                String.valueOf(transfer.getId()), null, toResponse(transfer), null);
        return toResponse(transfer);
    }

    @Transactional
    public TransferResponse approve(Long id, User approvedBy) {
        TransferRequest transfer = getOrThrow(id);
        assertStatus(transfer, TransferStatus.PENDING, "approve");

        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovedBy(approvedBy);
        transfer.setApprovalDate(Instant.now());
        transfer = transferRepository.save(transfer);

        auditService.logAsync(approvedBy, "APPROVE_TRANSFER", "TRANSFER",
                String.valueOf(id), null, null, null);
        return toResponse(transfer);
    }

    @Transactional
    public TransferResponse ship(Long id, User shippedBy) {
        TransferRequest transfer = getOrThrow(id);
        assertStatus(transfer, TransferStatus.APPROVED, "ship");

        Inventory originInventory = inventoryService.getInventoryOrThrow(
                transfer.getProduct().getId(), transfer.getOriginBranch().getId());

        if (originInventory.getCurrentStock().compareTo(transfer.getQuantity()) < 0) {
            throw new InsufficientStockException(
                    transfer.getProduct().getSku(),
                    transfer.getOriginBranch().getName(),
                    transfer.getQuantity(),
                    originInventory.getCurrentStock()
            );
        }

        inventoryService.createTransferMovement(
                originInventory, transfer.getQuantity(), MovementReason.TRANSFER_OUTBOUND,
                shippedBy, transfer);

        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setShipDate(Instant.now());
        transfer = transferRepository.save(transfer);

        auditService.logAsync(shippedBy, "SHIP_TRANSFER", "TRANSFER",
                String.valueOf(id), null, null, null);
        return toResponse(transfer);
    }

    @Transactional
    public TransferResponse receive(Long id, User receivedBy) {
        TransferRequest transfer = getOrThrow(id);
        assertStatus(transfer, TransferStatus.IN_TRANSIT, "receive");

        Inventory destInventory = inventoryService.getInventoryOrThrow(
                transfer.getProduct().getId(), transfer.getDestinationBranch().getId());

        inventoryService.createTransferMovement(
                destInventory, transfer.getQuantity(), MovementReason.TRANSFER_INBOUND,
                receivedBy, transfer);

        transfer.setStatus(TransferStatus.RECEIVED);
        transfer.setReceptionDate(Instant.now());
        transfer = transferRepository.save(transfer);

        auditService.logAsync(receivedBy, "RECEIVE_TRANSFER", "TRANSFER",
                String.valueOf(id), null, null, null);
        return toResponse(transfer);
    }

    @Transactional
    public TransferResponse cancel(Long id, User cancelledBy) {
        TransferRequest transfer = getOrThrow(id);
        if (transfer.getStatus() == TransferStatus.RECEIVED ||
            transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel a transfer with status " + transfer.getStatus());
        }
        transfer.setStatus(TransferStatus.CANCELLED);
        transfer = transferRepository.save(transfer);

        auditService.logAsync(cancelledBy, "CANCEL_TRANSFER", "TRANSFER",
                String.valueOf(id), null, null, null);
        return toResponse(transfer);
    }

    private TransferRequest getOrThrow(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id));
    }

    private void assertStatus(TransferRequest transfer, TransferStatus expected, String action) {
        if (transfer.getStatus() != expected) {
            throw new BusinessException(
                    "Cannot %s a transfer with status %s".formatted(action, transfer.getStatus()),
                    HttpStatus.CONFLICT
            );
        }
    }

    private TransferResponse toResponse(TransferRequest t) {
        return new TransferResponse(
                t.getId(),
                t.getOriginBranch().getId(), t.getOriginBranch().getName(),
                t.getDestinationBranch().getId(), t.getDestinationBranch().getName(),
                t.getProduct().getId(), t.getProduct().getSku(), t.getProduct().getName(),
                t.getQuantity(), t.getStatus(),
                t.getRequestedBy().getId(), t.getRequestedBy().getFullName(),
                t.getApprovedBy() != null ? t.getApprovedBy().getId() : null,
                t.getApprovedBy() != null ? t.getApprovedBy().getFullName() : null,
                t.getNotes(),
                t.getRequestDate(), t.getApprovalDate(), t.getShipDate(), t.getReceptionDate()
        );
    }
}
