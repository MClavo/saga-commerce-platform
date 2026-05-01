package com.mclavo.ecommerce.email;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.mclavo.ecommerce.order.Product;

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

    @Async
    public void sendPaymentSuccessEmail(
            String toEmail,
            String customerName,
            BigDecimal amount,
            String orderReference
    ) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("amount", amount);
        variables.put("orderReference", orderReference);

        sendEmail(toEmail, EmailTemplate.PAYMENT_CONFIRMATION, variables, "Payment success");
    }

    @Async
    public void sendOrderConfirmationEmail(
            String toEmail,
            String customerName,
            BigDecimal totalAmount,
            String orderReference,
            List<Product> products
    ) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("totalAmount", totalAmount);
        variables.put("orderReference", orderReference);
        variables.put("products", products);

        sendEmail(toEmail, EmailTemplate.ORDER_CONFIRMATION, variables, "Order confirmation");
    }

    private void sendEmail(
            String toEmail,
            EmailTemplate emailTemplate,
            Map<String, Object> variables,
            String emailType
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
            log.info("{} email sent to {}", emailType, toEmail);

        } catch (MessagingException e) {
            log.warn("Failed to send {} email to {}: {}", emailType.toLowerCase(), toEmail, e.getMessage());
        }
    }

}
