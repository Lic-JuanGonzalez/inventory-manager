package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.InventoryMovement;
import com.inventory.management.domain.enums.MovementType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;

public final class InventoryMovementSpecs {

    private InventoryMovementSpecs() {}

    public static Specification<InventoryMovement> withFilters(
            Long branchId, Long productId, Long userId,
            MovementType type, Instant from, Instant to) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("product", JoinType.INNER);
                root.fetch("branch", JoinType.INNER);
                root.fetch("user", JoinType.INNER);
                query.distinct(true);
            }

            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (branchId != null)  predicates.add(cb.equal(root.get("branch").get("id"), branchId));
            if (productId != null) predicates.add(cb.equal(root.get("product").get("id"), productId));
            if (userId != null)    predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (type != null)      predicates.add(cb.equal(root.get("type"), type));
            if (from != null)      predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
