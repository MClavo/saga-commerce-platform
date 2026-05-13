package com.mclavo.ecommerce.payment.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mclavo.ecommerce.payment.domain.PaymentMethod;
import com.mclavo.ecommerce.payment.domain.PaymentStatus;

public record PaymentResponse(
        Integer id,
        String paymentReference,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
