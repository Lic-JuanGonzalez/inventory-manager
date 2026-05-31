package com.inventory.management.service;

import com.inventory.management.domain.entity.*;
import com.inventory.management.domain.enums.MovementReason;
import com.inventory.management.domain.repository.InventoryMovementRepository;
import com.inventory.management.domain.repository.InventoryRepository;
import com.inventory.management.dto.request.InventoryMovementRequest;
import com.inventory.management.dto.response.MovementResponse;
import com.inventory.management.exception.InsufficientStockException;
import com.inventory.management.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository inventoryRepository;
    @Mock InventoryMovementRepository movementRepository;
    @Mock ProductService productService;
    @Mock BranchService branchService;
    @Mock AuditService auditService;

    @InjectMocks InventoryService inventoryService;

    private User user;
    private Product product;
    private Branch branch;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Op").lastName("Test").email("op@test.com").build();
        product = Product.builder().id(1L).sku("P-001").name("Prod").unitOfMeasure("UNIDAD")
                .referencePrice(new BigDecimal("10.00")).active(true).build();
        branch = Branch.builder().id(1L).name("Central").address("Addr").active(true).build();
        inventory = Inventory.builder().id(1L).product(product).branch(branch)
                .currentStock(new BigDecimal("50")).minStock(new BigDecimal("5"))
                .maxStock(new BigDecimal("100")).build();
    }

    @Test
    void registerMovement_entrada_increasesStock() {
        InventoryMovementRequest req = new InventoryMovementRequest(
                1L, 1L, MovementReason.COMPRA, new BigDecimal("10"), "Test compra");

        when(inventoryRepository.findByProductIdAndBranchId(1L, 1L)).thenReturn(Optional.of(inventory));
        InventoryMovement savedMovement = InventoryMovement.builder()
                .id(1L).type(com.inventory.management.domain.enums.MovementType.ENTRADA)
                .reason(MovementReason.COMPRA).quantity(new BigDecimal("10"))
                .stockBefore(new BigDecimal("50")).stockAfter(new BigDecimal("60"))
                .product(product).branch(branch).user(user).build();
        when(movementRepository.save(any())).thenReturn(savedMovement);
        when(inventoryRepository.save(any())).thenReturn(inventory);

        MovementResponse response = inventoryService.registerMovement(req, user);

        assertThat(inventory.getCurrentStock()).isEqualByComparingTo(new BigDecimal("60"));
        assertThat(response.type()).isEqualTo(com.inventory.management.domain.enums.MovementType.ENTRADA);
    }

    @Test
    void registerMovement_salida_decreasesStock() {
        InventoryMovementRequest req = new InventoryMovementRequest(
                1L, 1L, MovementReason.VENTA, new BigDecimal("20"), "Venta");

        when(inventoryRepository.findByProductIdAndBranchId(1L, 1L)).thenReturn(Optional.of(inventory));
        InventoryMovement savedMovement = InventoryMovement.builder()
                .id(2L).type(com.inventory.management.domain.enums.MovementType.SALIDA)
                .reason(MovementReason.VENTA).quantity(new BigDecimal("20"))
                .stockBefore(new BigDecimal("50")).stockAfter(new BigDecimal("30"))
                .product(product).branch(branch).user(user).build();
        when(movementRepository.save(any())).thenReturn(savedMovement);
        when(inventoryRepository.save(any())).thenReturn(inventory);

        MovementResponse response = inventoryService.registerMovement(req, user);

        assertThat(inventory.getCurrentStock()).isEqualByComparingTo(new BigDecimal("30"));
    }

    @Test
    void registerMovement_insufficientStock_throwsException() {
        InventoryMovementRequest req = new InventoryMovementRequest(
                1L, 1L, MovementReason.VENTA, new BigDecimal("100"), null);

        when(inventoryRepository.findByProductIdAndBranchId(1L, 1L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.registerMovement(req, user))
                .isInstanceOf(InsufficientStockException.class);

        verify(inventoryRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void registerMovement_noInventory_throwsNotFoundException() {
        InventoryMovementRequest req = new InventoryMovementRequest(
                99L, 99L, MovementReason.COMPRA, BigDecimal.ONE, null);

        when(inventoryRepository.findByProductIdAndBranchId(99L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.registerMovement(req, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
