package com.mclavo.ecommerce.order.api;

import java.util.List;

import com.mclavo.ecommerce.order.domain.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    Integer id,

    @NotBlank(message = "Order reference cannot be blank")
    String reference,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    @NotBlank(message = "Customer ID cannot be blank")
    String customerId,
    
    @Valid
    @NotEmpty(message = "Order must contain at least one product")
    List<OrderProductRequest> products

) { }
