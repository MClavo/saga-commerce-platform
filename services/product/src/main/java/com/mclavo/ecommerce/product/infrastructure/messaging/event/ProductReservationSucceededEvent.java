package com.mclavo.ecommerce.product.infrastructure.messaging.event;

import java.math.BigDecimal;

public record ProductReservationSucceededEvent(
        Integer orderId,
        String orderReference,
        BigDecimal totalAmount,
        String paymentMethod
) {
}
