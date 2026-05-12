package com.mclavo.ecommerce.exception;

public class DuplicateOrderReferenceException extends RuntimeException {

    public DuplicateOrderReferenceException(String message) {
        super(message);
    }
}
