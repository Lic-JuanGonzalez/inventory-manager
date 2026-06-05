package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.AuditLog;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;

public final class AuditLogSpecs {

    private AuditLogSpecs() {}

    public static Specification<AuditLog> withFilters(
            Long userId, String action, String entity,
            Instant from, Instant to) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("user", JoinType.LEFT);
                query.distinct(true);
            }

            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (userId != null)                          predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (action != null && !action.isBlank())     predicates.add(cb.like(cb.lower(root.get("action")), "%" + action.toLowerCase() + "%"));
            if (entity != null && !entity.isBlank())     predicates.add(cb.equal(root.get("entityName"), entity));
            if (from != null)                            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)                              predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
