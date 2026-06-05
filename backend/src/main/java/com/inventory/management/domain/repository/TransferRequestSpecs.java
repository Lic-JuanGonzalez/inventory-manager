package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.TransferRequest;
import com.inventory.management.domain.enums.TransferStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public final class TransferRequestSpecs {

    private TransferRequestSpecs() {}

    public static Specification<TransferRequest> withFilters(
            TransferStatus status, Long originBranchId,
            Long destinationBranchId, Long productId) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("originBranch", JoinType.INNER);
                root.fetch("destinationBranch", JoinType.INNER);
                root.fetch("product", JoinType.INNER);
                root.fetch("requestedBy", JoinType.INNER);
                query.distinct(true);
            }

            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (status != null)             predicates.add(cb.equal(root.get("status"), status));
            if (originBranchId != null)     predicates.add(cb.equal(root.get("originBranch").get("id"), originBranchId));
            if (destinationBranchId != null) predicates.add(cb.equal(root.get("destinationBranch").get("id"), destinationBranchId));
            if (productId != null)          predicates.add(cb.equal(root.get("product").get("id"), productId));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
