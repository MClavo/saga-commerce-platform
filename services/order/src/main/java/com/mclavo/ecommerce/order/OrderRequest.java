package com.mclavo.ecommerce.order;

import java.math.BigDecimal;
import java.util.List;

import com.mclavo.ecommerce.product.PurchaseRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

record OrderRequest(
    Integer id,
    String reference,
    // TODO: Amount should be calculated by the service, not provided by the client
    // WTF, WHY WOULD THE CLIENT PROVIDE THE AMOUNT? THIS IS A HUGE SECURITY RISK
    @Positive(message = "Order amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    @NotBlank(message = "Customer ID cannot be blank")
    String customerId,
    
    // WHY NOT USE A SET?
    @NotEmpty(message = "Order must contain at least one product")
    List<PurchaseRequest> products

) { }
