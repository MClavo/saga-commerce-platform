package com.mclavo.ecommerce.payment.application;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.payment.api.PaymentRequest;
import com.mclavo.ecommerce.payment.domain.Payment;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentRequestedEvent;

@Service
public class PaymentMapper {

    public Payment toPayment(PaymentRequest request) {
        return Payment.builder()
            .paymentReference("MANUAL-" + request.orderReference())
            .amount(request.amount())
            .orderId(request.orderId())
            .paymentMethod(request.paymentMethod())
            .build();

    }

    Payment toPayment(PaymentRequestedEvent event, String paymentReference) {
        return Payment.builder()
            .paymentReference(paymentReference)
            .amount(event.totalAmount())
            .orderId(event.orderId())
            .paymentMethod(event.paymentMethod())
            .build();
    }

}
