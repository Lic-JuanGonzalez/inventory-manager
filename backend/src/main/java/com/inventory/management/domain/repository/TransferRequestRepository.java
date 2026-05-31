package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.TransferRequest;
import com.inventory.management.domain.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

    long countByStatus(TransferStatus status);

    List<TransferRequest> findByStatus(TransferStatus status);

    @Query("""
            SELECT t FROM TransferRequest t
            JOIN FETCH t.originBranch
            JOIN FETCH t.destinationBranch
            JOIN FETCH t.product
            JOIN FETCH t.requestedBy
            WHERE ('' = :status OR t.status = :status)
            AND (:originBranchId IS NULL OR t.originBranch.id = :originBranchId)
            AND (:destinationBranchId IS NULL OR t.destinationBranch.id = :destinationBranchId)
            AND (:productId IS NULL OR t.product.id = :productId)
            """)
    Page<TransferRequest> findAllFiltered(@Param("status") String status,
                                          @Param("originBranchId") Long originBranchId,
                                          @Param("destinationBranchId") Long destinationBranchId,
                                          @Param("productId") Long productId,
                                          Pageable pageable);
}
