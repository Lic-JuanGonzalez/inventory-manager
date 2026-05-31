package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Branch> findAllByActiveTrue();

    @Query("""
            SELECT b FROM Branch b
            WHERE (:search IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%',:search,'%'))
                   OR LOWER(b.address) LIKE LOWER(CONCAT('%',:search,'%')))
            AND (:active IS NULL OR b.active = :active)
            """)
    Page<Branch> findAllFiltered(@Param("search") String search,
                                 @Param("active") Boolean active,
                                 Pageable pageable);
}
