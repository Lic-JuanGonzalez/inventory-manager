package com.inventory.management.controller;

import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.UserRepository;
import com.inventory.management.dto.request.CreateUserRequest;
import com.inventory.management.dto.request.UpdateUserRequest;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.dto.response.UserResponse;
import com.inventory.management.security.CustomUserDetails;
import com.inventory.management.service.UserService;
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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "Gestión de usuarios")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Listar usuarios con paginación y filtros")
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.findAll(search, active,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request,
                                               @AuthenticationPrincipal CustomUserDetails principal) {
        User currentUser = resolveUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateUserRequest request,
                                               @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(userService.update(id, request, resolveUser(principal)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario")
    public ResponseEntity<Void> deactivate(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails principal) {
        userService.deactivate(id, resolveUser(principal));
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(CustomUserDetails principal) {
        return userRepository.findById(principal.getUserId()).orElseThrow();
    }
}
