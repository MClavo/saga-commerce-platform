package com.mclavo.ecommerce.product.infrastructure.messaging.event;

import java.util.List;

public record ProductReservationSucceededEvent(
        Integer orderId,
        String orderReference,
        List<ProductReservationItem> products
) {
}
