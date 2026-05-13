package com.mclavo.ecommerce.payment.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mclavo.ecommerce.payment.application.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> findByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.findByOrderId(orderId));
    }

    @PostMapping("/demo/orders/{orderId}/confirm")
    public ResponseEntity<PaymentResponse> confirmDemoPayment(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.confirmDemoPayment(orderId));
    }

    @PostMapping("/demo/orders/{orderId}/fail")
    public ResponseEntity<PaymentResponse> failDemoPayment(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.failDemoPayment(orderId));
    }

}
