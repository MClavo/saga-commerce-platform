package com.mclavo.ecommerce.product.api;

import jakarta.validation.constraints.NotNull;

public record ProductStockAdjustmentRequest(
        @NotNull(message = "Stock adjustment quantity delta is required")
        Integer quantityDelta
) { }
