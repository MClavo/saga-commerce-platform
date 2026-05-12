package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import java.math.BigDecimal;

public record OrderItemSnapshot(
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
