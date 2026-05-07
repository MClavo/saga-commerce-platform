package com.mclavo.ecommerce.product.infrastructure.messaging.event;

public record ProductReservationFailedEvent(
        Integer orderId,
        String orderReference,
        String failureReason
) {
}
