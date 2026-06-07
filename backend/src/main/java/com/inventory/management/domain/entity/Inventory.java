package com.inventory.management.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_inventory_product_branch",
           columnNames = {"product_id", "branch_id"}
       ))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "current_stock", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "min_stock", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal minStock = BigDecimal.ZERO;

    @Column(name = "max_stock", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal maxStock = BigDecimal.ZERO;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isBelowMinStock() {
        return currentStock.compareTo(minStock) < 0;
    }

    public void addStock(BigDecimal quantity) {
        this.currentStock = this.currentStock.add(quantity);
    }

    public void subtractStock(BigDecimal quantity) {
        BigDecimal result = this.currentStock.subtract(quantity);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient stock. Current stock: " + this.currentStock);
        }
        this.currentStock = result;
    }
}
