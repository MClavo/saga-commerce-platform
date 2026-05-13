package com.mclavo.ecommerce.payment.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mclavo.ecommerce.exception.PaymentNotFoundException;
import com.mclavo.ecommerce.exception.PaymentStateConflictException;
import com.mclavo.ecommerce.payment.api.PaymentResponse;
import com.mclavo.ecommerce.payment.domain.Payment;
import com.mclavo.ecommerce.payment.domain.PaymentStatus;
import com.mclavo.ecommerce.payment.infrastucture.messaging.PaymentEventProducer;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentConfirmedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentFailedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentRequestedEvent;
import com.mclavo.ecommerce.payment.infrastucture.persistence.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public void processPaymentRequested(PaymentRequestedEvent event) {
        paymentRepository.save(paymentMapper.toPendingPayment(event));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(Integer id) {
        return paymentMapper.toResponse(findPaymentById(id));
    }

    @Transactional(readOnly = true)
    public PaymentResponse findByOrderId(Integer orderId) {
        return paymentMapper.toResponse(findPaymentByOrderId(orderId));
    }

    @Transactional
    public PaymentResponse confirmDemoPayment(Integer orderId) {
        Payment payment = findPaymentByOrderId(orderId);
        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new PaymentStateConflictException("Payment is already failed");
        }
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            return paymentMapper.toResponse(payment);
        }

        payment.setStatus(PaymentStatus.CONFIRMED);
        Payment savedPayment = paymentRepository.save(payment);
        paymentEventProducer.publishPaymentConfirmed(
                new PaymentConfirmedEvent(
                        savedPayment.getOrderId(),
                        orderReference(savedPayment),
                        savedPayment.getPaymentReference()));

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse failDemoPayment(Integer orderId) {
        Payment payment = findPaymentByOrderId(orderId);
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new PaymentStateConflictException("Payment is already confirmed");
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            return paymentMapper.toResponse(payment);
        }

        payment.setStatus(PaymentStatus.FAILED);
        Payment savedPayment = paymentRepository.save(payment);
        paymentEventProducer.publishPaymentFailed(
                new PaymentFailedEvent(
                        savedPayment.getOrderId(),
                        orderReference(savedPayment),
                        "Demo payment failure"));

        return paymentMapper.toResponse(savedPayment);
    }

    private Payment findPaymentById(Integer id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));
    }

    private Payment findPaymentByOrderId(Integer orderId) {
        return paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order ID: " + orderId));
    }

    private String orderReference(Payment payment) {
        return payment.getPaymentReference().replaceFirst("^PAY-", "");
    }

}
