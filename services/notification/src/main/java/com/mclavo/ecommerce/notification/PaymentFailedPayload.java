package com.mclavo.ecommerce.notification;

import java.math.BigDecimal;
import java.util.List;

public record PaymentFailedPayload(
        BigDecimal totalAmount,
        String paymentMethod,
        String failureReason,
        List<OrderItemSnapshot> products
) implements NotificationPayload {
}
