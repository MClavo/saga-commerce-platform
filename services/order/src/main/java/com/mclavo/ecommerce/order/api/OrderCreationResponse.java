package com.mclavo.ecommerce.order.api;

import com.mclavo.ecommerce.order.domain.OrderStatus;

public record OrderCreationResponse(
        Integer orderId,
        OrderStatus status
) {
}
