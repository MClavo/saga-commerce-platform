package com.mclavo.ecommerce.order.infrastructure.messaging.event;

public record NotificationRequestedEvent(
        Integer orderId,
        String orderReference,
        NotificationType notificationType,
        RecipientSnapshot recipient,
        NotificationPayload payload
) {
}
