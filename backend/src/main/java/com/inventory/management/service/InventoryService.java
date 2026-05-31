package com.inventory.management.service;

import com.inventory.management.domain.entity.*;
import com.inventory.management.domain.enums.MovementType;
import com.inventory.management.domain.repository.InventoryMovementRepository;
import com.inventory.management.domain.repository.InventoryRepository;
import com.inventory.management.dto.request.InitInventoryRequest;
import com.inventory.management.dto.request.InventoryMovementRequest;
import com.inventory.management.dto.response.*;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.exception.InsufficientStockException;
import com.inventory.management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductService productService;
    private final BranchService branchService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> findAll(Long branchId, Long productId,
                                                   Long categoryId, Boolean active, Pageable pageable) {
        return PageResponse.of(
                inventoryRepository.findAllFiltered(branchId, productId, categoryId, active, pageable)
                        .map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findLowStock() {
        return inventoryRepository.findLowStockItems().stream().map(this::toResponse).toList();
    }

    @Transactional
    public InventoryResponse initializeInventory(InitInventoryRequest req, User currentUser) {
        inventoryRepository.findByProductIdAndBranchId(req.productId(), req.branchId())
                .ifPresent(i -> { throw new BusinessException(
                        "Inventario ya existe para este producto y sucursal", HttpStatus.CONFLICT); });

        if (req.minStock().compareTo(req.maxStock()) > 0) {
            throw new BusinessException("Stock mínimo no puede ser mayor al stock máximo");
        }

        Product product = productService.getOrThrow(req.productId());
        Branch branch   = branchService.getOrThrow(req.branchId());

        Inventory inventory = Inventory.builder()
                .product(product)
                .branch(branch)
                .currentStock(req.currentStock())
                .minStock(req.minStock())
                .maxStock(req.maxStock())
                .build();
        inventory = inventoryRepository.save(inventory);
        auditService.logAsync(currentUser, "INIT_INVENTORY", "INVENTORY",
                String.valueOf(inventory.getId()), null, toResponse(inventory), null);
        return toResponse(inventory);
    }

    @Transactional
    public MovementResponse registerMovement(InventoryMovementRequest req, User currentUser) {
        MovementType expectedType = req.reason().getExpectedType();

        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(req.productId(), req.branchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe inventario para este producto en esta sucursal"));

        BigDecimal stockBefore = inventory.getCurrentStock();

        if (expectedType == MovementType.SALIDA) {
            if (inventory.getCurrentStock().compareTo(req.quantity()) < 0) {
                throw new InsufficientStockException(
                        inventory.getProduct().getSku(),
                        inventory.getBranch().getName(),
                        req.quantity(),
                        inventory.getCurrentStock()
                );
            }
            inventory.subtractStock(req.quantity());
        } else {
            inventory.addStock(req.quantity());
        }

        inventoryRepository.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .type(expectedType)
                .reason(req.reason())
                .quantity(req.quantity())
                .stockBefore(stockBefore)
                .stockAfter(inventory.getCurrentStock())
                .product(inventory.getProduct())
                .branch(inventory.getBranch())
                .user(currentUser)
                .observations(req.observations())
                .build();
        movement = movementRepository.save(movement);

        auditService.logAsync(currentUser, "INVENTORY_MOVEMENT", "INVENTORY_MOVEMENT",
                String.valueOf(movement.getId()), null, toMovementResponse(movement), null);

        return toMovementResponse(movement);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovementResponse> findMovements(Long branchId, Long productId, Long userId,
                                                        MovementType type, Instant from, Instant to,
                                                        Pageable pageable) {
        return PageResponse.of(
                movementRepository.findAllFiltered(branchId, productId, userId, type, from, to, pageable)
                        .map(this::toMovementResponse)
        );
    }

    InventoryMovement createTransferMovement(Inventory inventory, BigDecimal quantity,
                                              com.inventory.management.domain.enums.MovementReason reason,
                                              User user, TransferRequest transferRequest) {
        BigDecimal stockBefore = inventory.getCurrentStock();
        if (reason.getExpectedType() == MovementType.SALIDA) {
            inventory.subtractStock(quantity);
        } else {
            inventory.addStock(quantity);
        }
        inventoryRepository.save(inventory);

        return movementRepository.save(InventoryMovement.builder()
                .type(reason.getExpectedType())
                .reason(reason)
                .quantity(quantity)
                .stockBefore(stockBefore)
                .stockAfter(inventory.getCurrentStock())
                .product(inventory.getProduct())
                .branch(inventory.getBranch())
                .user(user)
                .transferRequest(transferRequest)
                .build());
    }

    Inventory getInventoryOrThrow(Long productId, Long branchId) {
        return inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe inventario para el producto en la sucursal especificada"));
    }

    public InventoryResponse toResponse(Inventory i) {
        BigDecimal stockValue = i.getCurrentStock().multiply(i.getProduct().getReferencePrice());
        return new InventoryResponse(
                i.getId(),
                i.getProduct().getId(), i.getProduct().getSku(), i.getProduct().getName(),
                i.getProduct().getCategory() != null ? i.getProduct().getCategory().getName() : null,
                i.getBranch().getId(), i.getBranch().getName(),
                i.getCurrentStock(), i.getMinStock(), i.getMaxStock(),
                i.getProduct().getUnitOfMeasure(), i.getProduct().getReferencePrice(),
                stockValue, i.isBelowMinStock(), i.getUpdatedAt()
        );
    }

    private MovementResponse toMovementResponse(InventoryMovement m) {
        return new MovementResponse(
                m.getId(), m.getType(), m.getReason(), m.getQuantity(),
                m.getStockBefore(), m.getStockAfter(),
                m.getProduct().getId(), m.getProduct().getSku(), m.getProduct().getName(),
                m.getBranch().getId(), m.getBranch().getName(),
                m.getUser().getId(), m.getUser().getFullName(),
                m.getTransferRequest() != null ? m.getTransferRequest().getId() : null,
                m.getObservations(), m.getCreatedAt()
        );
    }
}
