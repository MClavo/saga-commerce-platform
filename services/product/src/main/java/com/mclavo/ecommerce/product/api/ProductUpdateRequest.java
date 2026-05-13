package com.mclavo.ecommerce.product.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductUpdateRequest(
        @NotNull(message = "Product name is required")
        String name,

        @NotNull(message = "Product description is required")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price should be positive")
        BigDecimal price,

        @NotNull(message = "Product category is required")
        Integer categoryId
) { }
