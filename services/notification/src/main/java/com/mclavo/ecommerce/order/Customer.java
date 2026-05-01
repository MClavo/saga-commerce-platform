package com.mclavo.ecommerce.order;

public record Customer(
    String id,
    String firstname,
    String lastname,
    String email
) {}
