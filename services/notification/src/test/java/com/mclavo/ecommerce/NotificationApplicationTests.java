package com.mclavo.ecommerce;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.mclavo.ecommerce.email.EmailService;
import com.mclavo.ecommerce.notification.Notification;
import com.mclavo.ecommerce.notification.NotificationConsumer;
import com.mclavo.ecommerce.notification.NotificationRepository;
import com.mclavo.ecommerce.notification.NotificationRequestedEvent;
import com.mclavo.ecommerce.notification.NotificationType;
import com.mclavo.ecommerce.notification.OrderConfirmedPayload;
import com.mclavo.ecommerce.notification.OrderItemSnapshot;
import com.mclavo.ecommerce.notification.ProductReservationFailedPayload;
import com.mclavo.ecommerce.notification.RecipientSnapshot;

import jakarta.annotation.Resource;

@SpringBootTest(classes = {
        NotificationConsumer.class,
        NotificationApplicationTests.TestConfig.class
})
class NotificationApplicationTests {

    @Resource
    private NotificationConsumer notificationConsumer;

    @Resource
    private NotificationRepository notificationRepository;

    @Resource
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        reset(notificationRepository, emailService);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void should_Persist_And_Send_Email_When_NotificationRequestedEvent_IsConsumed() {
        var event = orderConfirmedEvent();

        notificationConsumer.consume(event);

        var notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertAll(
                () -> assertEquals(42, notification.getOrderId()),
                () -> assertEquals("ORD-42", notification.getOrderReference()),
                () -> assertEquals(NotificationType.ORDER_CONFIRMED, notification.getType()),
                () -> assertEquals("ada@example.com", notification.getRecipient().email()),
                () -> assertEquals(event.payload(), notification.getPayload()));

        verify(emailService).sendNotificationEmail(event);
    }

    @Test
    void should_Consume_Event_When_Email_Send_Fails() {
        var event = new NotificationRequestedEvent(
                42,
                "ORD-42",
                NotificationType.PRODUCT_RESERVATION_FAILED,
                recipient(),
                new ProductReservationFailedPayload("not enough stock"));
        doThrow(new IllegalStateException("mail server down"))
                .when(emailService).sendNotificationEmail(event);

        assertDoesNotThrow(() -> notificationConsumer.consume(event));

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendNotificationEmail(event);
    }

    private NotificationRequestedEvent orderConfirmedEvent() {
        return new NotificationRequestedEvent(
                42,
                "ORD-42",
                NotificationType.ORDER_CONFIRMED,
                recipient(),
                new OrderConfirmedPayload(
                        new BigDecimal("37.98"),
                        "CREDIT_CARD",
                        "PAY-42",
                        List.of(new OrderItemSnapshot(
                                1,
                                "Claw Hammer",
                                2,
                                new BigDecimal("18.99"),
                                new BigDecimal("37.98")))));
    }

    private RecipientSnapshot recipient() {
        return new RecipientSnapshot("customer-1", "Ada", "Lovelace", "ada@example.com");
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        NotificationRepository notificationRepository() {
            return mock(NotificationRepository.class);
        }

        @Bean
        EmailService emailService() {
            return mock(EmailService.class);
        }
    }
}
