package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmedPayload(
        BigDecimal totalAmount,
        String paymentMethod,
        String paymentReference,
        List<OrderItemSnapshot> products
) implements NotificationPayload {
}
