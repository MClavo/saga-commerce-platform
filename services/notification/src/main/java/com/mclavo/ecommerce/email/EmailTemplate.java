package com.mclavo.ecommerce.email;

import lombok.Getter;

public enum EmailTemplate {
    ORDER_CONFIRMED("order-confirmation.html", "Order confirmation"),
    PAYMENT_FAILED("payment-failed.html", "Payment failed"),
    PRODUCT_RESERVATION_FAILED("product-reservation-failed.html", "Product reservation failed");


    @Getter
    private final String template;
    @Getter
    private final String subject;

    EmailTemplate(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
