package com.mclavo.ecommerce.notification;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmedPayload(
        BigDecimal totalAmount,
        String paymentMethod,
        String paymentReference,
        List<OrderItemSnapshot> products
) implements NotificationPayload {
}
