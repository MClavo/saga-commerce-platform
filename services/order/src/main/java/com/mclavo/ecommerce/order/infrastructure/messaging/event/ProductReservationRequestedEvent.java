package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProductReservationRequestedEvent(
        @NotNull(message = "Order ID is required")
        Integer orderId,

        @NotBlank(message = "Order reference is required")
        String orderReference,

        @Valid
        @NotEmpty(message = "Order products are required")
        List<OrderProductItem> products
) {
}
