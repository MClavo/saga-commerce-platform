package com.mclavo.ecommerce.payment.application;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.payment.api.PaymentResponse;
import com.mclavo.ecommerce.payment.domain.Payment;
import com.mclavo.ecommerce.payment.domain.PaymentStatus;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentRequestedEvent;

@Service
public class PaymentMapper {

    Payment toPendingPayment(PaymentRequestedEvent event) {
        return Payment.builder()
                .paymentReference("PAY-" + event.orderReference())
                .amount(event.totalAmount())
                .orderId(event.orderId())
                .paymentMethod(event.paymentMethod())
                .status(PaymentStatus.PENDING)
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }

}
