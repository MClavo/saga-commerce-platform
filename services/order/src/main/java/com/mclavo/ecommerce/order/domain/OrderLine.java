package com.mclavo.ecommerce.order.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "customer_line")
public class OrderLine {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    /**
     * Creates a requested line before Product Service confirms product name and price.
     */
    OrderLine(Order order, Integer productId, Integer quantity) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(productId, "Product id cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
    }

    /**
     * Applies the confirmed Product Service snapshot to this line.
     *
     * <p>The quantity must match the original request because Product Service may
     * confirm or reject the requested reservation, but it must not silently change
     * the order being paid for.</p>
     */
    void applySnapshot(OrderProductSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "Product snapshot cannot be null");

        if (!productId.equals(snapshot.productId())) {
            throw new IllegalStateException(
                    "Product snapshot does not match order line product: " + productId);
        }

        if (!quantity.equals(snapshot.quantity())) {
            throw new IllegalStateException(
                    "Reserved quantity does not match requested quantity for product: " + productId);
        }

        this.productName = snapshot.productName();
        this.unitPrice = snapshot.unitPrice();
        this.subtotal = snapshot.unitPrice()
                .multiply(BigDecimal.valueOf(snapshot.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    boolean hasProductSnapshot() {
        return productName != null && unitPrice != null && subtotal != null;
    }
}
