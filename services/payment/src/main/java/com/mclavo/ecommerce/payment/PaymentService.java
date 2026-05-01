package com.mclavo.ecommerce.payment;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.notification.NotificationProducer;
import com.mclavo.ecommerce.notification.PaymentNotificationRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationProducer notificationProducer;

    public Payment createPayment(PaymentRequest request) {

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

        return payment;
    }

}
