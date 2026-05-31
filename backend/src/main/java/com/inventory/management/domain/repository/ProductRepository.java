package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.category c
            WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:search,'%'))
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%',:search,'%')))
            AND (:categoryId IS NULL OR c.id = :categoryId)
            AND (:active IS NULL OR p.active = :active)
            """)
    Page<Product> findAllFiltered(@Param("search") String search,
                                  @Param("categoryId") Long categoryId,
                                  @Param("active") Boolean active,
                                  Pageable pageable);
}
