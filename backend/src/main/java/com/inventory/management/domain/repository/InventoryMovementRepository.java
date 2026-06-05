package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long>,
        JpaSpecificationExecutor<InventoryMovement> {

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
