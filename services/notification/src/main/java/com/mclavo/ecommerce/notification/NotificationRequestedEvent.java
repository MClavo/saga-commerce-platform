package com.mclavo.ecommerce.notification;

/**
 * Local copy of the integration event produced by Order Service on
 * notification.requested. Event records are duplicated per service in this repo,
 * so JSON shape must stay aligned with Order Service.
 */
public record NotificationRequestedEvent(
        Integer orderId,
        String orderReference,
        NotificationType notificationType,
        RecipientSnapshot recipient,
        NotificationPayload payload
) {
}
