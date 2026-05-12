package com.mclavo.ecommerce.notification;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "${application.kafka.notification-requested-topic}")
    public void consume(NotificationRequestedEvent event) {
        log.info("Consuming notification requested event for order reference: {}", event.orderReference());

        notificationRepository.save(Notification.from(event, LocalDateTime.now()));

        try {
            emailService.sendNotificationEmail(event);
        } catch (RuntimeException e) {
            log.warn(
                    "Notification email failed for order reference {} and type {}: {}",
                    event.orderReference(),
                    event.notificationType(),
                    e.getMessage());
        }
    }
}
