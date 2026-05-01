package com.mclavo.ecommerce.notification;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.email.EmailService;
import com.mclavo.ecommerce.order.OrderConfirmation;
import com.mclavo.ecommerce.payment.PaymentConfirmation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "${application.kafka.payment-topic}")
    public void consumePaymentSuccessNotification(PaymentConfirmation paymentConfirmation) {
        log.info("Consuming payment confirmation: {}", paymentConfirmation);

        notificationRepository.save(
            Notification.builder()
                .type(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .paymentConfirmation(paymentConfirmation)
                .build()
        );

        // Send email
        String name = paymentConfirmation.customerFirstName() + " " + paymentConfirmation.customerLastName();

        emailService.sendPaymentSuccessEmail(
            paymentConfirmation.customerEmail(),
            name,
            paymentConfirmation.amount(),
            paymentConfirmation.orderReference()
        );
    }

    @KafkaListener(topics = "${application.kafka.order-topic}")
    public void consumeOrderNotification(OrderConfirmation orderConfirmation) {
        log.info("Consuming order confirmation: {}", orderConfirmation);

        notificationRepository.save(
            Notification.builder()
                .type(NotificationType.ORDER_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .orderConfirmation(orderConfirmation)
                .build()
        );

        // Send email
        String name = orderConfirmation.customer().firstname() + " " + orderConfirmation.customer().lastname();
        emailService.sendOrderConfirmationEmail(
            orderConfirmation.customer().email(),
            name,
            orderConfirmation.totalAmount(),
            orderConfirmation.orderReference(),
            orderConfirmation.products()
        );
    }
    
}
