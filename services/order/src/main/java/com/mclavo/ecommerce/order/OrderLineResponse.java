package com.mclavo.ecommerce.order;

import java.math.BigDecimal;

public record OrderLineResponse(
        Integer id,
        Integer productId,
        Integer quantity,
        BigDecimal unitPrice
) {}
