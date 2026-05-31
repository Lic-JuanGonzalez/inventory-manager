package com.inventory.management.controller;

import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.UserRepository;
import com.inventory.management.dto.request.CreateBranchRequest;
import com.inventory.management.dto.response.BranchResponse;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.security.CustomUserDetails;
import com.inventory.management.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Gestión de sucursales")
public class BranchController {

    private final BranchService branchService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Listar sucursales con paginación")
    public ResponseEntity<PageResponse<BranchResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(branchService.findAll(search, active,
                PageRequest.of(page, size, Sort.by("name"))));
    }

    @GetMapping("/active")
    @Operation(summary = "Listar sucursales activas")
    public ResponseEntity<List<BranchResponse>> findAllActive() {
        return ResponseEntity.ok(branchService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sucursal por ID")
    public ResponseEntity<BranchResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear sucursal")
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchService.create(request, resolveUser(principal)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sucursal")
    public ResponseEntity<BranchResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody CreateBranchRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(branchService.update(id, request, resolveUser(principal)));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Activar/desactivar sucursal")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails principal) {
        branchService.toggleActive(id, resolveUser(principal));
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(CustomUserDetails p) {
        return userRepository.findById(p.getUserId()).orElseThrow();
    }
}
