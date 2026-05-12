package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.List;

public record PaymentFailedPayload(
        BigDecimal totalAmount,
        String paymentMethod,
        String failureReason,
        List<OrderItemSnapshot> products
) implements NotificationPayload {
}
