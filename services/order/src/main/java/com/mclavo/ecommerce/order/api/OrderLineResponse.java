package com.mclavo.ecommerce.order.api;

import java.math.BigDecimal;

public record OrderLineResponse(
        Integer id,
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
) {}
