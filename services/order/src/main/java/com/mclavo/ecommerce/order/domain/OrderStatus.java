package com.mclavo.ecommerce.order.domain;

public enum OrderStatus {
    PRODUCT_RESERVATION_PENDING,
    AWAITING_PAYMENT,
    CONFIRMED,
    PRODUCT_RESERVATION_FAILED,
    PAYMENT_FAILED
}
