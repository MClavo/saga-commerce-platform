package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import java.util.List;

public record ProductReservationSucceededEvent(
        Integer orderId,
        String orderReference,
        List<ProductReservationItem> products
) {
}
