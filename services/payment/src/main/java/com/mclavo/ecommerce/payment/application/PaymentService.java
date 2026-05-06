package com.mclavo.ecommerce.payment.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mclavo.ecommerce.notification.NotificationProducer;
import com.mclavo.ecommerce.notification.PaymentNotificationRequest;
import com.mclavo.ecommerce.payment.api.PaymentRequest;
import com.mclavo.ecommerce.payment.infrastucture.gateway.PaymentGateway;
import com.mclavo.ecommerce.payment.infrastucture.messaging.PaymentEventProducer;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.OrderCreatedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentProcessedEvent;
import com.mclavo.ecommerce.payment.infrastucture.persistence.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationProducer notificationProducer;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public Integer createPayment(PaymentRequest request) {

        var payment = paymentRepository.save(paymentMapper.toPayment(request));

        // Send notification via kafka to notification service
        notificationProducer.sendPaymentNotification(
                new PaymentNotificationRequest(
                        request.orderReference(),
                        request.amount(),
                        request.paymentMethod(),
                        request.customer().firstname(),
                        request.customer().lastname(),
                        request.customer().email()
                )
        );

        return payment.getId();
    }

    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {
        // Payment processing logic (stubbed for this example)
        String paymentReference = paymentGateway.process(event);

        // Save payment details to the database
        paymentRepository.save(paymentMapper.toPayment(event, paymentReference));

        // Create and send payment notification to notification service
        var customer = event.customer();
        notificationProducer.sendPaymentNotification(
                new PaymentNotificationRequest(
                        event.orderReference(),
                        event.totalAmount(),
                        event.paymentMethod(),
                        customer.firstname(),
                        customer.lastname(),
                        customer.email()
                )
        );

        // Notify other services about successful payment processing
        paymentEventProducer.publishPaymentProcessed(
                new PaymentProcessedEvent(
                        event.orderId(),
                        event.orderReference(),
                        paymentReference));
    }

}
