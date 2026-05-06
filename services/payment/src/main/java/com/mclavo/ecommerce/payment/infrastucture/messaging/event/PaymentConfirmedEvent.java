package com.mclavo.ecommerce.payment.infrastucture.messaging.event;

public record PaymentConfirmedEvent(
        Integer orderId,
        String orderReference,
        String paymentReference
) {
}
