package com.inventory.management.controller;

import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.UserRepository;
import com.inventory.management.dto.request.CreateProductRequest;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.dto.response.ProductResponse;
import com.inventory.management.security.CustomUserDetails;
import com.inventory.management.service.ProductService;
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

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List products with filters and pagination")
    public ResponseEntity<PageResponse<ProductResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.findAll(search, categoryId, active,
                PageRequest.of(page, size, Sort.by("name"))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create product")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request,
                                                   @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request, resolveUser(principal)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CreateProductRequest request,
                                                   @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(productService.update(id, request, resolveUser(principal)));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle product active status")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails principal) {
        productService.toggleActive(id, resolveUser(principal));
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(CustomUserDetails p) {
        return userRepository.findById(p.getUserId()).orElseThrow();
    }
}
