package com.mclavo.ecommerce.product.api;

import java.math.BigDecimal;

public record ProductPurchaseResponse(
    Integer productId,
    String name,
    String description,
    BigDecimal price,
    Integer quantity
) {
}
