package com.mclavo.ecommerce.order;

import java.math.BigDecimal;
import java.util.List;

import com.mclavo.ecommerce.customer.CustomerResponse;
import com.mclavo.ecommerce.product.PurchaseResponse;

record OrderConfirmation(
    String orderReference,
    BigDecimal totalAmount,
    PaymentMethod paymentMethod,
    CustomerResponse customer,
    List<PurchaseResponse> products
) {}
