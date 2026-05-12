package com.mclavo.ecommerce.order.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mclavo.ecommerce.order.application.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreationResponse> createOrder(
            @RequestBody @Valid OrderRequest request) {
        OrderCreationResponse response = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .header("Location", "/api/v1/orders/" + response.orderId())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/{id}/order-lines")
    public ResponseEntity<List<OrderLineResponse>> findOrderLinesByOrderId(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.findOrderLinesByOrderId(id));
    }
}
