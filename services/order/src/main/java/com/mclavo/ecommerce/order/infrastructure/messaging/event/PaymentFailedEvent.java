package com.mclavo.ecommerce.order.infrastructure.messaging.event;

public record PaymentFailedEvent(
        Integer orderId,
        String orderReference,
        String failureReason
) {
}
