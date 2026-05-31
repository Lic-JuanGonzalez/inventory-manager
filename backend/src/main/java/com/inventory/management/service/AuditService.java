package com.inventory.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.domain.entity.AuditLog;
import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.AuditLogRepository;
import com.inventory.management.dto.response.AuditLogResponse;
import com.inventory.management.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(User user, String action, String entityName, String entityId,
                         Object oldValues, Object newValues, String ipAddress) {
        try {
            AuditLog audit = AuditLog.builder()
                    .user(user)
                    .userEmail(user != null ? user.getEmail() : null)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .oldValues(toJson(oldValues))
                    .newValues(toJson(newValues))
                    .ipAddress(ipAddress)
                    .status("SUCCESS")
                    .build();
            auditLogRepository.save(audit);
        } catch (Exception e) {
            log.error("Error saving audit log for action {} on {}: {}", action, entityName, e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(String userEmail, String action, String entityName,
                           String errorMessage, String ipAddress) {
        try {
            AuditLog audit = AuditLog.builder()
                    .userEmail(userEmail)
                    .action(action)
                    .entityName(entityName)
                    .status("FAILURE")
                    .errorMessage(errorMessage)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(audit);
        } catch (Exception e) {
            log.error("Error saving failure audit log: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findAll(Long userId, String action, String entity,
                                                  Instant from, Instant to, Pageable pageable) {
        return PageResponse.of(
                auditLogRepository.findAllFiltered(userId,
                        action == null ? "" : action,
                        entity == null ? "" : entity,
                        from, to, pageable)
                        .map(this::toResponse)
        );
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getUser() != null ? a.getUser().getId() : null,
                a.getUserEmail(),
                a.getAction(),
                a.getEntityName(),
                a.getEntityId(),
                a.getOldValues(),
                a.getNewValues(),
                a.getIpAddress(),
                a.getStatus(),
                a.getErrorMessage(),
                a.getCreatedAt()
        );
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
