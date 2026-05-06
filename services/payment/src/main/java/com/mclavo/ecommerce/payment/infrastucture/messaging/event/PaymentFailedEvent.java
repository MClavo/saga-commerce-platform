package com.mclavo.ecommerce.payment.infrastucture.messaging.event;

public record PaymentFailedEvent(
        Integer orderId,
        String orderReference,
        String failureReason
) {
}
