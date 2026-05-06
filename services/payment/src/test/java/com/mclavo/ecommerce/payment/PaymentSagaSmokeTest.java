package com.mclavo.ecommerce.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import com.mclavo.ecommerce.config.KafkaPaymentProperties;
import com.mclavo.ecommerce.payment.application.PaymentMapper;
import com.mclavo.ecommerce.payment.application.PaymentService;
import com.mclavo.ecommerce.payment.domain.Payment;
import com.mclavo.ecommerce.payment.domain.PaymentMethod;
import com.mclavo.ecommerce.payment.infrastucture.gateway.PaymentGateway;
import com.mclavo.ecommerce.payment.infrastucture.gateway.StubPaymentGateway;
import com.mclavo.ecommerce.payment.infrastucture.messaging.PaymentEventProducer;
import com.mclavo.ecommerce.payment.infrastucture.messaging.ProductReservationSucceededConsumer;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentConfirmedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.ProductReservationSucceededEvent;
import com.mclavo.ecommerce.payment.infrastucture.persistence.PaymentRepository;

import jakarta.annotation.Resource;

@SpringBootTest(classes = {
        ProductReservationSucceededConsumer.class,
        PaymentService.class,
        PaymentMapper.class,
        PaymentEventProducer.class,
        PaymentSagaSmokeTest.TestConfig.class
})
class PaymentSagaSmokeTest {

    @Resource
    private ProductReservationSucceededConsumer consumer;

    @Resource
    private PaymentRepository paymentRepository;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void should_Process_Payment_And_PublishEvent_when_ProductReservationSucceededEvent_Consumed() {
        
        // given
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var event = new ProductReservationSucceededEvent(
                42,
                "ORD-42",
                new BigDecimal("99.90"),
                PaymentMethod.CREDIT_CARD);

        // when
        consumer.consume(event);

        // then
        var paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment payment = paymentCaptor.getValue();

        assertEquals(42, payment.getOrderId());
        assertEquals(new BigDecimal("99.90"), payment.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD, payment.getPaymentMethod());
        assertEquals("PAY-ORD-42", payment.getPaymentReference());

        var eventCaptor = ArgumentCaptor.forClass(PaymentConfirmedEvent.class);
        verify(kafkaTemplate).send(eq("payment.confirmed"), eq("ORD-42"), eventCaptor.capture());

        assertEquals(42, eventCaptor.getValue().orderId());
        assertEquals("ORD-42", eventCaptor.getValue().orderReference());
        assertEquals("PAY-ORD-42", eventCaptor.getValue().paymentReference());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        KafkaPaymentProperties kafkaPaymentProperties() {
            return new KafkaPaymentProperties(
                    "product.reservation.succeeded",
                    "payment.confirmed",
                    "payment.failed");
        }

        @Bean
        PaymentRepository paymentRepository() {
            return mock(PaymentRepository.class);
        }

        @Bean
        PaymentGateway paymentGateway() {
            return new StubPaymentGateway();
        }

        @Bean
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }
}
