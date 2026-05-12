package com.mclavo.ecommerce.order.infrastructure.messaging.event;

public record ProductReservationFailedPayload(
        String failureReason
) implements NotificationPayload {
}
