package com.mclavo.ecommerce.order.domain;

public enum OrderStatus {
    PRODUCT_RESERVATION_PENDING,
    PRODUCT_RESERVED,
    CONFIRMED,
    PRODUCT_RESERVATION_FAILED,
    PAYMENT_FAILED
}
