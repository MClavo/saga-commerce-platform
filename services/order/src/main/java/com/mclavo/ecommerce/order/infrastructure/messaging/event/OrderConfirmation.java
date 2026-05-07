package com.mclavo.ecommerce.order.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.List;

import com.mclavo.ecommerce.customer.CustomerResponse;
import com.mclavo.ecommerce.order.domain.PaymentMethod;
import com.mclavo.ecommerce.product.PurchaseResponse;

public record OrderConfirmation(
    String orderReference,
    BigDecimal totalAmount,
    PaymentMethod paymentMethod,
    CustomerResponse customer,
    List<PurchaseResponse> products
) {}
