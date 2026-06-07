package com.inventory.management.service;

import com.inventory.management.domain.entity.*;
import com.inventory.management.domain.enums.TransferStatus;
import com.inventory.management.domain.repository.TransferRequestRepository;
import com.inventory.management.dto.request.CreateTransferRequest;
import com.inventory.management.dto.response.TransferResponse;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.exception.InsufficientStockException;
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
class TransferServiceTest {

    @Mock TransferRequestRepository transferRepository;
    @Mock ProductService productService;
    @Mock BranchService branchService;
    @Mock InventoryService inventoryService;
    @Mock AuditService auditService;

    @InjectMocks TransferService transferService;

    private User requestUser;
    private Product product;
    private Branch origin, destination;
    private Inventory originInventory;

    @BeforeEach
    void setUp() {
        requestUser = User.builder().id(1L).name("Op").lastName("Test").email("op@test.com").build();
        product = Product.builder().id(1L).sku("P-001").name("Prod").unitOfMeasure("UNIDAD")
                .referencePrice(BigDecimal.TEN).active(true).build();
        origin = Branch.builder().id(1L).name("Origin").address("Addr A").active(true).build();
        destination = Branch.builder().id(2L).name("Dest").address("Addr B").active(true).build();
        originInventory = Inventory.builder().id(1L).product(product).branch(origin)
                .currentStock(new BigDecimal("50")).minStock(BigDecimal.ZERO)
                .maxStock(new BigDecimal("100")).build();
    }

    @Test
    void create_withSufficientStock_createsTransfer() {
        CreateTransferRequest req = new CreateTransferRequest(1L, 2L, 1L, new BigDecimal("10"), null);

        when(branchService.getOrThrow(1L)).thenReturn(origin);
        when(branchService.getOrThrow(2L)).thenReturn(destination);
        when(productService.getOrThrow(1L)).thenReturn(product);
        when(inventoryService.getInventoryOrThrow(1L, 1L)).thenReturn(originInventory);
        TransferRequest saved = TransferRequest.builder()
                .id(1L).originBranch(origin).destinationBranch(destination)
                .product(product).quantity(new BigDecimal("10"))
                .requestedBy(requestUser).status(TransferStatus.PENDING).build();
        when(transferRepository.save(any())).thenReturn(saved);

        TransferResponse response = transferService.create(req, requestUser);

        assertThat(response.status()).isEqualTo(TransferStatus.PENDING);
        assertThat(response.originBranchId()).isEqualTo(1L);
    }

    @Test
    void create_withSameBranches_throwsBusinessException() {
        CreateTransferRequest req = new CreateTransferRequest(1L, 1L, 1L, BigDecimal.ONE, null);

        assertThatThrownBy(() -> transferService.create(req, requestUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("different");
    }

    @Test
    void create_withInsufficientStock_throwsException() {
        CreateTransferRequest req = new CreateTransferRequest(1L, 2L, 1L, new BigDecimal("200"), null);

        when(branchService.getOrThrow(1L)).thenReturn(origin);
        when(branchService.getOrThrow(2L)).thenReturn(destination);
        when(productService.getOrThrow(1L)).thenReturn(product);
        when(inventoryService.getInventoryOrThrow(1L, 1L)).thenReturn(originInventory);

        assertThatThrownBy(() -> transferService.create(req, requestUser))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void approve_withPendingTransfer_approvesSuccessfully() {
        User approver = User.builder().id(2L).name("Admin").lastName("A").email("admin@test.com").build();
        TransferRequest transfer = TransferRequest.builder()
                .id(1L).originBranch(origin).destinationBranch(destination)
                .product(product).quantity(BigDecimal.TEN)
                .requestedBy(requestUser).status(TransferStatus.PENDING).build();

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any())).thenReturn(transfer);

        TransferResponse response = transferService.approve(1L, approver);

        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.APPROVED);
        assertThat(transfer.getApprovedBy()).isEqualTo(approver);
    }

    @Test
    void approve_alreadyApproved_throwsBusinessException() {
        TransferRequest transfer = TransferRequest.builder()
                .id(1L).originBranch(origin).destinationBranch(destination)
                .product(product).quantity(BigDecimal.TEN)
                .requestedBy(requestUser).status(TransferStatus.APPROVED).build();

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThatThrownBy(() -> transferService.approve(1L, requestUser))
                .isInstanceOf(BusinessException.class);
    }
}
