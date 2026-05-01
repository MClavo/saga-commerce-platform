package com.mclavo.ecommerce.payment;

import java.math.BigDecimal;

record PaymentRequest(
    BigDecimal amount,
    PaymentMethod paymentMethod,
    Integer orderId,
    String orderReference,
    Customer customer

) {}
