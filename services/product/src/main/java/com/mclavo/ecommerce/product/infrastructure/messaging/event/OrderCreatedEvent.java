package com.mclavo.ecommerce.product.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreatedEvent(
        @NotNull(message = "Order ID is required")
        Integer orderId,

        @NotBlank(message = "Order reference is required")
        String orderReference,

        @NotNull(message = "Total amount is required")
        @Positive(message = "Total amount must be positive")
        BigDecimal totalAmount,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @Valid
        @NotEmpty(message = "Order products are required")
        List<OrderProductItem> products
) {
}
