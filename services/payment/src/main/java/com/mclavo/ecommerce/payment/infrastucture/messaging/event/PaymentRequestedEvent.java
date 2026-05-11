package com.mclavo.ecommerce.payment.infrastucture.messaging.event;

import java.math.BigDecimal;

import com.mclavo.ecommerce.payment.domain.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequestedEvent(
        @NotNull(message = "Order ID is required")
        Integer orderId,

        @NotBlank(message = "Order reference is required")
        String orderReference,

        @NotNull(message = "Total amount is required")
        @Positive(message = "Total amount must be positive")
        BigDecimal totalAmount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
