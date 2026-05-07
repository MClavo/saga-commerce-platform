package com.mclavo.ecommerce.payment;

import java.math.BigDecimal;

import com.mclavo.ecommerce.customer.CustomerResponse;
import com.mclavo.ecommerce.order.domain.PaymentMethod;

public record PaymentRequest(
    BigDecimal amount,
    PaymentMethod paymentMethod,
    Integer orderId,
    String orderReference,
    CustomerResponse customer

) {}
