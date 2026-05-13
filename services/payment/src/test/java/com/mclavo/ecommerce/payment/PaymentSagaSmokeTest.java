package com.mclavo.ecommerce.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import com.mclavo.ecommerce.config.KafkaPaymentProperties;
import com.mclavo.ecommerce.exception.PaymentNotFoundException;
import com.mclavo.ecommerce.exception.PaymentStateConflictException;
import com.mclavo.ecommerce.payment.application.PaymentMapper;
import com.mclavo.ecommerce.payment.application.PaymentService;
import com.mclavo.ecommerce.payment.domain.Payment;
import com.mclavo.ecommerce.payment.domain.PaymentMethod;
import com.mclavo.ecommerce.payment.domain.PaymentStatus;
import com.mclavo.ecommerce.payment.infrastucture.gateway.PaymentGateway;
import com.mclavo.ecommerce.payment.infrastucture.gateway.StubPaymentGateway;
import com.mclavo.ecommerce.payment.infrastucture.messaging.PaymentEventProducer;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentConfirmedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentFailedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.PaymentRequestedConsumer;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentRequestedEvent;
import com.mclavo.ecommerce.payment.infrastucture.persistence.PaymentRepository;

import jakarta.annotation.Resource;

@SpringBootTest(classes = {
        PaymentRequestedConsumer.class,
        PaymentService.class,
        PaymentMapper.class,
        PaymentEventProducer.class,
        PaymentSagaSmokeTest.TestConfig.class
})
class PaymentSagaSmokeTest {

    @Resource
    private PaymentRequestedConsumer consumer;

    @Resource
    private PaymentRepository paymentRepository;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Resource
    private PaymentService paymentService;

    @BeforeEach
    void resetMocks() {
        reset(paymentRepository, kafkaTemplate);
    }

    @Test
    void should_Create_Pending_Payment_when_PaymentRequestedEvent_Consumed() {

        // given
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var event = new PaymentRequestedEvent(
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
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        verify(kafkaTemplate, never()).send(eq("payment.confirmed"), eq("ORD-42"), any());
    }

    @Test
    void should_Confirm_Payment_And_PublishEvent_when_DemoPaymentConfirmed() {

        // given
        Payment payment = Payment.builder()
                .id(1)
                .orderId(42)
                .paymentReference("PAY-ORD-42")
                .amount(new BigDecimal("99.90"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(42))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = paymentService.confirmDemoPayment(42);

        // then
        assertEquals(PaymentStatus.CONFIRMED, response.status());

        var eventCaptor = ArgumentCaptor.forClass(PaymentConfirmedEvent.class);
        verify(kafkaTemplate).send(eq("payment.confirmed"), eq("ORD-42"), eventCaptor.capture());

        assertEquals(42, eventCaptor.getValue().orderId());
        assertEquals("ORD-42", eventCaptor.getValue().orderReference());
        assertEquals("PAY-ORD-42", eventCaptor.getValue().paymentReference());
    }

    @Test
    void should_Fail_Payment_And_PublishEvent_when_DemoPaymentFailed() {

        // given
        Payment payment = Payment.builder()
                .id(1)
                .orderId(42)
                .paymentReference("PAY-ORD-42")
                .amount(new BigDecimal("99.90"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(42))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = paymentService.failDemoPayment(42);

        // then
        assertEquals(PaymentStatus.FAILED, response.status());

        var eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(kafkaTemplate).send(eq("payment.failed"), eq("ORD-42"), eventCaptor.capture());

        assertEquals(42, eventCaptor.getValue().orderId());
        assertEquals("ORD-42", eventCaptor.getValue().orderReference());
        assertEquals("Demo payment failure", eventCaptor.getValue().failureReason());
    }

    @Test
    void should_Throw_NotFoundException_when_DemoPaymentMissing() {

        // given
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(42))
                .thenReturn(Optional.empty());

        // when / then
        assertThrows(PaymentNotFoundException.class, () -> paymentService.confirmDemoPayment(42));
    }

    @Test
    void should_Throw_StateConflictException_when_ConfirmedPaymentFailed() {

        // given
        Payment payment = Payment.builder()
                .id(1)
                .orderId(42)
                .paymentReference("PAY-ORD-42")
                .amount(new BigDecimal("99.90"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.CONFIRMED)
                .build();
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(42))
                .thenReturn(Optional.of(payment));

        // when / then
        assertThrows(PaymentStateConflictException.class, () -> paymentService.failDemoPayment(42));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        KafkaPaymentProperties kafkaPaymentProperties() {
            return new KafkaPaymentProperties(
                    "payment.requested",
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
