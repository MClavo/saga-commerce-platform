package com.mclavo.ecommerce.order.infrastructure.messaging.event;

public record PaymentConfirmedEvent(
        Integer orderId,
        String orderReference,
        String paymentReference
) {
}
