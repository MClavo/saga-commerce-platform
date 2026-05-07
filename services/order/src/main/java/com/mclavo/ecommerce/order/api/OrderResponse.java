package com.mclavo.ecommerce.order.api;

import java.math.BigDecimal;

import com.mclavo.ecommerce.order.domain.OrderStatus;
import com.mclavo.ecommerce.order.domain.PaymentMethod;

public record OrderResponse(
    Integer id,
    String reference,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    String customerId,
    OrderStatus status
) { }
