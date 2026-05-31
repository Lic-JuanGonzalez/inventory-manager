package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a FROM AuditLog a
            LEFT JOIN FETCH a.user
            WHERE (:userId IS NULL OR a.user.id = :userId)
            AND (:action IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%',:action,'%')))
            AND (:entity IS NULL OR a.entityName = :entity)
            AND (:from IS NULL OR a.createdAt >= :from)
            AND (:to IS NULL OR a.createdAt <= :to)
            """)
    Page<AuditLog> findAllFiltered(@Param("userId") Long userId,
                                   @Param("action") String action,
                                   @Param("entity") String entity,
                                   @Param("from") Instant from,
                                   @Param("to") Instant to,
                                   Pageable pageable);
}
