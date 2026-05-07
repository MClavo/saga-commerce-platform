package com.mclavo.ecommerce.order.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mclavo.ecommerce.customer.CustomerClient;
import com.mclavo.ecommerce.exception.BusinessException;
import com.mclavo.ecommerce.order.api.OrderLineResponse;
import com.mclavo.ecommerce.order.api.OrderRequest;
import com.mclavo.ecommerce.order.api.OrderResponse;
import com.mclavo.ecommerce.order.domain.Order;
import com.mclavo.ecommerce.order.infrastructure.messaging.OrderProducer;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderConfirmation;
import com.mclavo.ecommerce.order.infrastructure.persistence.OrderLineRepository;
import com.mclavo.ecommerce.order.infrastructure.persistence.OrderRepository;
import com.mclavo.ecommerce.payment.PaymentClient;
import com.mclavo.ecommerce.payment.PaymentRequest;
import com.mclavo.ecommerce.product.ProductClient;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final OrderMapper orderMapper;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;

    // What happens if payment fails? Do we need to roll back the order creation?
    // Do we need to implement a saga pattern for this?

    // TODO: Idempotency: If the client retries the request, we should not create
    // duplicate orders. We can use the reference field for this.
    @Transactional
    public Integer createOrder(OrderRequest request) {
        // Validate that the customer exists openfeign client to customer service
        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException(
                        "Order failed: Customer not found with ID: " + request.customerId()));

        // purchase the products (RestClient)
        var purchasedProducts = productClient.purchaseProducts(request.products());

        // Create order
        Order order = orderMapper.toOrder(request);

        // Create order lines from the purchased products
        for (var product : purchasedProducts) {
            order.addOrderLine(
                    product.productId(),
                    product.quantity(),
                    product.price());
        }

        // Save the order and order lines in the database
        Order savedOrder = orderRepository.save(order);

        // Request payment (feign client)
        paymentClient.requestOrderPayment(
                new PaymentRequest(
                        savedOrder.getTotalAmount(),
                        savedOrder.getPaymentMethod(),
                        savedOrder.getId(),
                        savedOrder.getReference(),
                        customer));

        // Send order confirmation (Kafka)
        orderProducer.publishOrderConfirmation(
                new OrderConfirmation(
                        savedOrder.getReference(),
                        savedOrder.getTotalAmount(),
                        savedOrder.getPaymentMethod(),
                        customer,
                        purchasedProducts));

        return savedOrder.getId();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    public OrderResponse findById(Integer id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order not found with ID: " + id));
        return orderMapper.toOrderResponse(order);
    }

    public List<OrderLineResponse> findOrderLinesByOrderId(Integer orderId) {
        return orderLineRepository.findAllByOrderId(orderId);
    }

}
