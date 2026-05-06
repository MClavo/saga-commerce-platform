package com.mclavo.ecommerce.payment.infrastucture.messaging.event;

public record PaymentProcessedEvent(
        Integer orderId,
        String orderReference,
        String paymentReference
) {
}
