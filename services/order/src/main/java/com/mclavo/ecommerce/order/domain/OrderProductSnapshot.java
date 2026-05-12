package com.mclavo.ecommerce.order.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Product data confirmed by Product Service during stock reservation.
 *
 * <p>Order Service deliberately treats this as a domain value object instead of
 * depending on Kafka event records inside the aggregate. These values become the
 * commercial source of truth for order lines, order total, and payment amount.</p>
 */
public record OrderProductSnapshot(
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
) {

    public OrderProductSnapshot {
        Objects.requireNonNull(productId, "Product id cannot be null");
        Objects.requireNonNull(productName, "Product name cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
}
