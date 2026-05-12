package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductReservationItem(
        @NotNull(message = "Product ID is required")
        Integer productId,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Product quantity is required")
        @Positive(message = "Product quantity must be positive")
        Integer quantity,

        @NotNull(message = "Product unit price is required")
        @Positive(message = "Product unit price must be positive")
        BigDecimal unitPrice
) {
}
