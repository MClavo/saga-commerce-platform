package com.mclavo.ecommerce.notification;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    private Integer orderId;
    private String orderReference;
    private NotificationType type;
    private RecipientSnapshot recipient;
    private NotificationPayload payload;
    private LocalDateTime notificationDate;

    public static Notification from(NotificationRequestedEvent event, LocalDateTime notificationDate) {
        return Notification.builder()
                .orderId(event.orderId())
                .orderReference(event.orderReference())
                .type(event.notificationType())
                .recipient(event.recipient())
                .payload(event.payload())
                .notificationDate(notificationDate)
                .build();
    }
}
