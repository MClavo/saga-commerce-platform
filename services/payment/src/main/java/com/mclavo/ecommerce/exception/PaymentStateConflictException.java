package com.mclavo.ecommerce.exception;

public class PaymentStateConflictException extends RuntimeException {

    public PaymentStateConflictException(String message) {
        super(message);
    }
}
