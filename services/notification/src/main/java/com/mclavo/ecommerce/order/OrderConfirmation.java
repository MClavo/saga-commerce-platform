package com.mclavo.ecommerce.order;

import java.math.BigDecimal;
import java.util.List;


import com.mclavo.ecommerce.payment.PaymentMethod;

public record OrderConfirmation(
    String orderReference,
    BigDecimal totalAmount,
    PaymentMethod paymentMethod,
    Customer customer,
    List<Product> products

) {}
