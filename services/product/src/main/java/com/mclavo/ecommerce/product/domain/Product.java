package com.mclavo.ecommerce.product.domain;

import java.math.BigDecimal;

import com.mclavo.ecommerce.exception.ProductPurchaseException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Product {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;
    private String description;
    @Column(nullable = false)
    private Integer availableQuantity;
    @Builder.Default
    @Column(nullable = false)
    private Integer reservedQuantity = 0;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public void reserveStock(Integer quantity) {
        validatePositive(quantity);
        if (availableQuantity < quantity) {
            throw new ProductPurchaseException("Insufficient stock for product with id: " + id);
        }

        availableQuantity -= quantity;
        reservedQuantity = normalizedReservedQuantity() + quantity;
    }

    public void commitReservedStock(Integer quantity) {
        validatePositive(quantity);
        if (normalizedReservedQuantity() < quantity) {
            throw new ProductPurchaseException("Insufficient reserved stock for product with id: " + id);
        }

        reservedQuantity -= quantity;
    }

    public void releaseReservedStock(Integer quantity) {
        validatePositive(quantity);
        if (normalizedReservedQuantity() < quantity) {
            throw new ProductPurchaseException("Insufficient reserved stock for product with id: " + id);
        }

        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    private Integer normalizedReservedQuantity() {
        return reservedQuantity == null ? 0 : reservedQuantity;
    }

    private void validatePositive(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ProductPurchaseException("Product quantity must be positive");
        }
    }

}
