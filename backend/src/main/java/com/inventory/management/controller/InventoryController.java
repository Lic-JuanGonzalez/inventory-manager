package com.inventory.management.controller;

import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.enums.MovementType;
import com.inventory.management.domain.repository.UserRepository;
import com.inventory.management.dto.request.InitInventoryRequest;
import com.inventory.management.dto.request.InventoryMovementRequest;
import com.inventory.management.dto.response.*;
import com.inventory.management.security.CustomUserDetails;
import com.inventory.management.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory control and movements")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List inventory with filters")
    public ResponseEntity<PageResponse<InventoryResponse>> findAll(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventoryService.findAll(branchId, productId, categoryId, active,
                PageRequest.of(page, size)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Products below minimum stock")
    public ResponseEntity<List<InventoryResponse>> getLowStock() {
        return ResponseEntity.ok(inventoryService.findLowStock());
    }

    @PostMapping("/initialize")
    @Operation(summary = "Initialize inventory for a product in a branch")
    public ResponseEntity<InventoryResponse> initialize(@Valid @RequestBody InitInventoryRequest request,
                                                        @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.initializeInventory(request, resolveUser(principal)));
    }

    @PostMapping("/movement")
    @Operation(summary = "Register inventory movement (inbound or outbound)")
    public ResponseEntity<MovementResponse> registerMovement(
            @Valid @RequestBody InventoryMovementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.registerMovement(request, resolveUser(principal)));
    }

    @GetMapping("/movements")
    @Operation(summary = "Movement history with filters")
    public ResponseEntity<PageResponse<MovementResponse>> getMovements(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventoryService.findMovements(branchId, productId, userId, type, from, to,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    private User resolveUser(CustomUserDetails p) {
        return userRepository.findById(p.getUserId()).orElseThrow();
    }
}
