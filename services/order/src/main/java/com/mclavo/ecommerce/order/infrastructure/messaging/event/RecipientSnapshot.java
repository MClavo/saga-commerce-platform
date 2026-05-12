package com.mclavo.ecommerce.order.infrastructure.messaging.event;

public record RecipientSnapshot(
        String customerId,
        String firstName,
        String lastName,
        String email
) {
}
