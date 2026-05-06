package com.mclavo.ecommerce.notification;

import java.math.BigDecimal;

import com.mclavo.ecommerce.payment.domain.PaymentMethod;

public record PaymentNotificationRequest(
    String orderReference,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    String customerFirstname,
    String customerLastname,
    String customerEmail
) {}
