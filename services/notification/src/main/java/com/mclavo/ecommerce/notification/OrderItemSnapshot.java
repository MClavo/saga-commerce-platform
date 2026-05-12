package com.mclavo.ecommerce.notification;

import java.math.BigDecimal;

public record OrderItemSnapshot(
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
