package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.InventoryMovement;
import com.inventory.management.domain.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @Query("""
            SELECT m FROM InventoryMovement m
            JOIN FETCH m.product
            JOIN FETCH m.branch
            JOIN FETCH m.user
            WHERE (:branchId IS NULL OR m.branch.id = :branchId)
            AND (:productId IS NULL OR m.product.id = :productId)
            AND (:userId IS NULL OR m.user.id = :userId)
            AND (:type IS NULL OR m.type = :type)
            AND (:from IS NULL OR m.createdAt >= :from)
            AND (:to IS NULL OR m.createdAt <= :to)
            """)
    Page<InventoryMovement> findAllFiltered(@Param("branchId") Long branchId,
                                            @Param("productId") Long productId,
                                            @Param("userId") Long userId,
                                            @Param("type") MovementType type,
                                            @Param("from") Instant from,
                                            @Param("to") Instant to,
                                            Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM InventoryMovement m
            WHERE m.createdAt >= :from AND m.createdAt <= :to
            """)
    long countByDateRange(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT m.product.id, m.product.name, COUNT(m) as movements
            FROM InventoryMovement m
            WHERE m.createdAt >= :from
            GROUP BY m.product.id, m.product.name
            ORDER BY movements DESC
            """)
    List<Object[]> findTopMovedProducts(@Param("from") Instant from, Pageable pageable);
}
