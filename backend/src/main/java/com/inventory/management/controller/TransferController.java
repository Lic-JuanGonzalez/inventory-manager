package com.inventory.management.controller;

import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.enums.TransferStatus;
import com.inventory.management.domain.repository.UserRepository;
import com.inventory.management.dto.request.CreateTransferRequest;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.dto.response.TransferResponse;
import com.inventory.management.security.CustomUserDetails;
import com.inventory.management.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Inventory transfers between branches")
public class TransferController {

    private final TransferService transferService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List transfers with filters")
    public ResponseEntity<PageResponse<TransferResponse>> findAll(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) Long originBranchId,
            @RequestParam(required = false) Long destinationBranchId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transferService.findAll(status, originBranchId, destinationBranchId,
                productId, PageRequest.of(page, size, Sort.by("requestDate").descending())));
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending transfers")
    public ResponseEntity<List<TransferResponse>> findPending() {
        return ResponseEntity.ok(transferService.findPending());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<TransferResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Request transfer")
    public ResponseEntity<TransferResponse> create(@Valid @RequestBody CreateTransferRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.create(request, resolveUser(principal)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve transfer (ADMIN only)")
    public ResponseEntity<TransferResponse> approve(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transferService.approve(id, resolveUser(principal)));
    }

    @PutMapping("/{id}/ship")
    @Operation(summary = "Mark transfer as shipped and deduct origin stock")
    public ResponseEntity<TransferResponse> ship(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transferService.ship(id, resolveUser(principal)));
    }

    @PutMapping("/{id}/receive")
    @Operation(summary = "Confirm receipt and add destination stock")
    public ResponseEntity<TransferResponse> receive(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transferService.receive(id, resolveUser(principal)));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel transfer")
    public ResponseEntity<TransferResponse> cancel(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transferService.cancel(id, resolveUser(principal)));
    }

    private User resolveUser(CustomUserDetails p) {
        return userRepository.findById(p.getUserId()).orElseThrow();
    }
}
