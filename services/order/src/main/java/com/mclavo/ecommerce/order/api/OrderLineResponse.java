package com.mclavo.ecommerce.order.api;

import java.math.BigDecimal;

public record OrderLineResponse(
        Integer id,
        Integer productId,
        Integer quantity,
        BigDecimal unitPrice
) {}
