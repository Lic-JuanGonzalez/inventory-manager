package com.inventory.management.controller;

import com.inventory.management.dto.response.AuditLogResponse;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
@Tag(name = "Audit", description = "Operation audit log")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Query audit logs with filters")
    public ResponseEntity<PageResponse<AuditLogResponse>> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditService.findAll(userId, action, entity, from, to,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}
