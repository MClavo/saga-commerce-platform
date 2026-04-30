package com.mclavo.ecommerce.order;

import java.math.BigDecimal;

public record OrderLineResponse(
        Integer id,
        Integer productId,
        Double quantity,
        BigDecimal unitPrice
) {}
