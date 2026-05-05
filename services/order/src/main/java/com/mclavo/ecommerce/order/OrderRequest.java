package com.mclavo.ecommerce.order;

import java.util.List;

import com.mclavo.ecommerce.product.PurchaseRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record OrderRequest(
    Integer id,
    String reference,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    @NotBlank(message = "Customer ID cannot be blank")
    String customerId,
    
    // WHY NOT USE A SET?
    @NotEmpty(message = "Order must contain at least one product")
    List<PurchaseRequest> products

) { }
