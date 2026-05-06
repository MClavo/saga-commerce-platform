package com.mclavo.ecommerce.payment.api;

import java.math.BigDecimal;

import com.mclavo.ecommerce.payment.domain.Customer;
import com.mclavo.ecommerce.payment.domain.PaymentMethod;

public record PaymentRequest(
    BigDecimal amount,
    PaymentMethod paymentMethod,
    Integer orderId,
    String orderReference,
    Customer customer

) {}
