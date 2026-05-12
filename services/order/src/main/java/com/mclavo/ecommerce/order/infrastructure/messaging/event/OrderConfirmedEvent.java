package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderConfirmedEvent(
        @NotNull(message = "Order ID is required")
        Integer orderId,

        @NotBlank(message = "Order reference is required")
        String orderReference
) {
}
