package com.mclavo.ecommerce.payment.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mclavo.ecommerce.payment.api.PaymentRequest;
import com.mclavo.ecommerce.payment.infrastucture.gateway.PaymentGateway;
import com.mclavo.ecommerce.payment.infrastucture.messaging.PaymentEventProducer;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentConfirmedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.ProductReservationSucceededEvent;
import com.mclavo.ecommerce.payment.infrastucture.persistence.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public Integer createPayment(PaymentRequest request) {

        var payment = paymentRepository.save(paymentMapper.toPayment(request));

        return payment.getId();
    }

    @Transactional
    public void processProductReservationSucceeded(ProductReservationSucceededEvent event) {
        String paymentReference = paymentGateway.process(event);

        paymentRepository.save(paymentMapper.toPayment(event, paymentReference));

        paymentEventProducer.publishPaymentConfirmed(
                new PaymentConfirmedEvent(
                        event.orderId(),
                        event.orderReference(),
                        paymentReference));
    }

}
