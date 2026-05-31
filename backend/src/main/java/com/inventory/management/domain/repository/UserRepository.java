package com.inventory.management.domain.repository;

import com.inventory.management.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndActiveTrue(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%',:search,'%'))
                   OR LOWER(u.lastName) LIKE LOWER(CONCAT('%',:search,'%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%',:search,'%')))
            AND (:active IS NULL OR u.active = :active)
            """)
    Page<User> findAllFiltered(@Param("search") String search,
                               @Param("active") Boolean active,
                               Pageable pageable);
}
