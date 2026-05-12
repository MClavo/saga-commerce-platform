package com.mclavo.ecommerce.email;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.mclavo.ecommerce.notification.NotificationRequestedEvent;
import com.mclavo.ecommerce.notification.OrderConfirmedPayload;
import com.mclavo.ecommerce.notification.PaymentFailedPayload;
import com.mclavo.ecommerce.notification.ProductReservationFailedPayload;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * Routes one business notification event to one customer email. Failures are
     * logged and swallowed because this service has no retry/DLQ policy yet.
     */
    public void sendNotificationEmail(NotificationRequestedEvent event) {
        switch (event.notificationType()) {
            case ORDER_CONFIRMED -> sendOrderConfirmedEmail(event, (OrderConfirmedPayload) event.payload());
            case PAYMENT_FAILED -> sendPaymentFailedEmail(event, (PaymentFailedPayload) event.payload());
            case PRODUCT_RESERVATION_FAILED -> sendProductReservationFailedEmail(
                    event,
                    (ProductReservationFailedPayload) event.payload());
        }
    }

    private void sendOrderConfirmedEmail(NotificationRequestedEvent event, OrderConfirmedPayload payload) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", event.recipient().fullName());
        variables.put("totalAmount", payload.totalAmount());
        variables.put("paymentMethod", payload.paymentMethod());
        variables.put("paymentReference", payload.paymentReference());
        variables.put("orderReference", event.orderReference());
        variables.put("products", payload.products());

        sendEmail(event.recipient().email(), EmailTemplate.ORDER_CONFIRMED, variables, event);
    }

    private void sendPaymentFailedEmail(NotificationRequestedEvent event, PaymentFailedPayload payload) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", event.recipient().fullName());
        variables.put("totalAmount", payload.totalAmount());
        variables.put("paymentMethod", payload.paymentMethod());
        variables.put("failureReason", payload.failureReason());
        variables.put("orderReference", event.orderReference());
        variables.put("products", payload.products());

        sendEmail(event.recipient().email(), EmailTemplate.PAYMENT_FAILED, variables, event);
    }

    private void sendProductReservationFailedEmail(
            NotificationRequestedEvent event,
            ProductReservationFailedPayload payload) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", event.recipient().fullName());
        variables.put("failureReason", payload.failureReason());
        variables.put("orderReference", event.orderReference());

        sendEmail(event.recipient().email(), EmailTemplate.PRODUCT_RESERVATION_FAILED, variables, event);
    }

    private void sendEmail(
            String toEmail,
            EmailTemplate emailTemplate,
            Map<String, Object> variables,
            NotificationRequestedEvent event
    ) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            messageHelper.setFrom("noreply@ecommerce.com");
            messageHelper.setTo(toEmail);
            messageHelper.setSubject(emailTemplate.getSubject());

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(emailTemplate.getTemplate(), context);
            messageHelper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("{} email sent to {}", event.notificationType(), toEmail);

        } catch (MessagingException | RuntimeException e) {
            log.warn(
                    "Failed to send {} email for order reference {} to {}: {}",
                    event.notificationType(),
                    event.orderReference(),
                    toEmail,
                    e.getMessage());
        }
    }
}
