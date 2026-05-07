package com.mclavo.ecommerce.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "product_reservation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_reservation_order_product",
                columnNames = {"order_id", "product_id"})
)
public class ProductReservation {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private String orderReference;
    @Column(nullable = false)
    private Integer productId;
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductReservationStatus status;

    public boolean isReserved() {
        return status == ProductReservationStatus.RESERVED;
    }

    public void commit() {
        if (isReserved()) {
            status = ProductReservationStatus.COMMITTED;
        }
    }

    public void release() {
        if (isReserved()) {
            status = ProductReservationStatus.RELEASED;
        }
    }
}
