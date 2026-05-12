package com.mclavo.ecommerce.notification;

public record ProductReservationFailedPayload(
        String failureReason
) implements NotificationPayload {
}
