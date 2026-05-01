package com.mclavo.ecommerce.payment;

public class PaymentMapper {

    public Payment toPayment(PaymentRequest request) {
        return Payment.builder()
            .amount(request.amount())
            .orderId(request.orderId())
            .paymentMethod(request.paymentMethod())
            .build();

    }

}
