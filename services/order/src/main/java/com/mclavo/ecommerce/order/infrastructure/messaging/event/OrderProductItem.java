package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderProductItem(
        @NotNull(message = "Product ID is required")
        Integer productId,

        @NotNull(message = "Product quantity is required")
        @Positive(message = "Product quantity must be positive")
        Integer quantity
) {
}
