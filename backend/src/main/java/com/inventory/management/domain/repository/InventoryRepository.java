package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndBranchId(Long productId, Long branchId);

    List<Inventory> findByBranchId(Long branchId);

    List<Inventory> findByProductId(Long productId);

    @Query("""
            SELECT i FROM Inventory i
            JOIN FETCH i.product p
            JOIN FETCH i.branch b
            LEFT JOIN FETCH p.category
            WHERE i.currentStock <= i.minStock
            AND b.active = true AND p.active = true
            ORDER BY i.currentStock ASC
            """)
    List<Inventory> findLowStockItems();

    @Query("""
            SELECT i FROM Inventory i
            JOIN FETCH i.product p
            JOIN FETCH i.branch b
            LEFT JOIN FETCH p.category
            WHERE (:branchId IS NULL OR b.id = :branchId)
            AND (:productId IS NULL OR p.id = :productId)
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (:active IS NULL OR p.active = :active)
            """)
    Page<Inventory> findAllFiltered(@Param("branchId") Long branchId,
                                    @Param("productId") Long productId,
                                    @Param("categoryId") Long categoryId,
                                    @Param("active") Boolean active,
                                    Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(i.currentStock * p.referencePrice), 0)
            FROM Inventory i
            JOIN i.product p
            WHERE (:branchId IS NULL OR i.branch.id = :branchId)
            """)
    java.math.BigDecimal calculateTotalValue(@Param("branchId") Long branchId);
}
