package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.TransferRequest;
import com.inventory.management.domain.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long>,
        JpaSpecificationExecutor<TransferRequest> {

    long countByStatus(TransferStatus status);

    List<TransferRequest> findByStatus(TransferStatus status);
}
