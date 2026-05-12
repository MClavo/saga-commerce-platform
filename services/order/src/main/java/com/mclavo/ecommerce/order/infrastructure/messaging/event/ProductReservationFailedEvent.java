package com.mclavo.ecommerce.order.infrastructure.messaging.event;

public record ProductReservationFailedEvent(
        Integer orderId,
        String orderReference,
        String failureReason
) {
}
