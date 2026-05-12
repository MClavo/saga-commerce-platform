package com.mclavo.ecommerce.order.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mclavo.ecommerce.customer.CustomerClient;
import com.mclavo.ecommerce.exception.BusinessException;
import com.mclavo.ecommerce.exception.DuplicateOrderReferenceException;
import com.mclavo.ecommerce.order.api.OrderCreationResponse;
import com.mclavo.ecommerce.order.api.OrderLineResponse;
import com.mclavo.ecommerce.order.api.OrderProductRequest;
import com.mclavo.ecommerce.order.api.OrderRequest;
import com.mclavo.ecommerce.order.api.OrderResponse;
import com.mclavo.ecommerce.order.domain.Order;
import com.mclavo.ecommerce.order.domain.OrderProductSnapshot;
import com.mclavo.ecommerce.order.domain.OrderStatus;
import com.mclavo.ecommerce.order.infrastructure.messaging.OrderProducer;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.NotificationRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.NotificationType;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderCancelledEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderConfirmedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderConfirmedPayload;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderItemSnapshot;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderProductItem;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentConfirmedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentFailedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentFailedPayload;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationFailedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationFailedPayload;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationItem;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationSucceededEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.RecipientSnapshot;
import com.mclavo.ecommerce.order.infrastructure.persistence.OrderLineRepository;
import com.mclavo.ecommerce.order.infrastructure.persistence.OrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerClient customerClient;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final OrderMapper orderMapper;
    private final OrderProducer orderProducer;

    /**
     * Starts the orchestrated saga.
     *
     * <p>Only customer validation is synchronous. Product details and prices are not
     * fetched here; they arrive later through product.reservation.succeeded and are
     * then used to calculate the order total and payment amount.</p>
     */
    @Transactional
    public OrderCreationResponse createOrder(OrderRequest request) {
        var requestedProducts = aggregateRequestedProducts(request.products());

        var existingOrder = orderRepository.findByReference(request.reference());
        if (existingOrder.isPresent()) {
            Order order = existingOrder.get();
            if (!sameCreateRequest(order, request, requestedProducts)) {
                throw new DuplicateOrderReferenceException(
                        "Order reference already exists with a different request: " + request.reference());
            }
            return new OrderCreationResponse(order.getId(), order.getStatus());
        }

        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException(
                        "Order failed: Customer not found with ID: " + request.customerId()));

        Order order = orderMapper.toOrder(request, customer);
        requestedProducts.forEach(product -> order.addRequestedLine(product.productId(), product.quantity()));

        Order savedOrder = orderRepository.save(order);
        orderProducer.publishProductReservationRequested(toProductReservationRequestedEvent(savedOrder));

        return new OrderCreationResponse(savedOrder.getId(), savedOrder.getStatus());
    }

    /**
     * Handles Product Service's successful reservation result and advances the saga
     * to payment.
     *
     * <p>The loaded Order is managed inside this transaction. Mutating the aggregate
     * is enough for Hibernate dirty checking; explicit saves are only needed for new
     * aggregate creation.</p>
     */
    @Transactional
    public void reserveProducts(ProductReservationSucceededEvent event) {
        Order order = findOrderForSaga(event.orderId());
        if (order.getStatus() == OrderStatus.PRODUCT_RESERVED
                || order.getStatus() == OrderStatus.CONFIRMED
                || order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            return;
        }

        ensureStatus(order, OrderStatus.PRODUCT_RESERVATION_PENDING);
        var snapshots = toOrderProductSnapshots(event.products());
        validateReservedProducts(order, snapshots);

        order.applyProductSnapshots(snapshots);
        order.markProductsReserved();

        orderProducer.publishPaymentRequested(new PaymentRequestedEvent(
                order.getId(),
                order.getReference(),
                order.getTotalAmount(),
                order.getPaymentMethod()));
    }

    /**
     * Stops the saga before payment when Product Service cannot reserve stock.
     */
    @Transactional
    public void failProductReservation(ProductReservationFailedEvent event) {
        Order order = findOrderForSaga(event.orderId());
        if (order.getStatus() == OrderStatus.PRODUCT_RESERVATION_FAILED) {
            return;
        }

        ensureStatus(order, OrderStatus.PRODUCT_RESERVATION_PENDING);
        order.markProductReservationFailed();

        orderProducer.publishNotificationRequested(new NotificationRequestedEvent(
                order.getId(),
                order.getReference(),
                NotificationType.PRODUCT_RESERVATION_FAILED,
                toRecipientSnapshot(order),
                new ProductReservationFailedPayload(event.failureReason())));
    }

    /**
     * Finalizes a paid order and emits the external confirmation events.
     */
    @Transactional
    public void confirmOrder(PaymentConfirmedEvent event) {
        Order order = findOrderForSaga(event.orderId());
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return;
        }

        ensureStatus(order, OrderStatus.PRODUCT_RESERVED);
        order.confirm();

        orderProducer.publishOrderConfirmed(new OrderConfirmedEvent(order.getId(), order.getReference()));
        orderProducer.publishNotificationRequested(new NotificationRequestedEvent(
                order.getId(),
                order.getReference(),
                NotificationType.ORDER_CONFIRMED,
                toRecipientSnapshot(order),
                new OrderConfirmedPayload(
                        order.getTotalAmount(),
                        order.getPaymentMethod().name(),
                        event.paymentReference(),
                        toOrderItemSnapshots(order))));
    }

    /**
     * Records payment failure and emits order.cancelled for Product Service stock
     * compensation. The internal status remains PAYMENT_FAILED.
     */
    @Transactional
    public void failPayment(PaymentFailedEvent event) {
        Order order = findOrderForSaga(event.orderId());
        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            return;
        }

        ensureStatus(order, OrderStatus.PRODUCT_RESERVED);
        order.markPaymentFailed();

        orderProducer.publishOrderCancelled(new OrderCancelledEvent(order.getId(), order.getReference()));
        orderProducer.publishNotificationRequested(new NotificationRequestedEvent(
                order.getId(),
                order.getReference(),
                NotificationType.PAYMENT_FAILED,
                toRecipientSnapshot(order),
                new PaymentFailedPayload(
                        order.getTotalAmount(),
                        order.getPaymentMethod().name(),
                        event.failureReason(),
                        toOrderItemSnapshots(order))));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Integer id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order not found with ID: " + id));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderLineResponse> findOrderLinesByOrderId(Integer orderId) {
        return orderLineRepository.findAllByOrderId(orderId);
    }

    private List<OrderProductItem> aggregateRequestedProducts(List<OrderProductRequest> requestedProducts) {
        Map<Integer, Integer> quantitiesByProductId = new LinkedHashMap<>();
        requestedProducts.forEach(product -> quantitiesByProductId.merge(
                product.productId(),
                product.quantity(),
                Integer::sum));

        return quantitiesByProductId.entrySet().stream()
                .map(entry -> new OrderProductItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Same reference means idempotent retry only when the business request matches.
     */
    private boolean sameCreateRequest(
            Order order,
            OrderRequest request,
            List<OrderProductItem> requestedProducts) {

        return order.getCustomerId().equals(request.customerId())
                && order.getPaymentMethod() == request.paymentMethod()
                && sameRequestedProducts(order, requestedProducts);
    }

    private boolean sameRequestedProducts(Order order, List<OrderProductItem> requestedProducts) {
        Map<Integer, Integer> storedProducts = new LinkedHashMap<>();
        order.getOrderLines().forEach(line -> storedProducts.put(line.getProductId(), line.getQuantity()));

        Map<Integer, Integer> newProducts = new LinkedHashMap<>();
        requestedProducts.forEach(product -> newProducts.put(product.productId(), product.quantity()));

        return storedProducts.equals(newProducts);
    }

    private void validateReservedProducts(Order order, List<OrderProductSnapshot> reservedProducts) {
        Map<Integer, Integer> requestedProducts = new LinkedHashMap<>();
        order.getOrderLines().forEach(line -> requestedProducts.put(line.getProductId(), line.getQuantity()));

        Map<Integer, Integer> reservedProductQuantities = new LinkedHashMap<>();
        reservedProducts.forEach(product -> reservedProductQuantities.put(product.productId(), product.quantity()));

        if (!requestedProducts.equals(reservedProductQuantities)) {
            throw new IllegalStateException("Reserved products do not match requested products for order: "
                    + order.getId());
        }
    }

    private List<OrderProductSnapshot> toOrderProductSnapshots(List<ProductReservationItem> products) {
        return products.stream()
                .map(product -> new OrderProductSnapshot(
                        product.productId(),
                        product.productName(),
                        product.quantity(),
                        product.unitPrice()))
                .toList();
    }

    /**
     * Saga transitions use a pessimistic lock so competing Kafka events for the same
     * order cannot both observe the same prior status and publish conflicting next
     * events.
     */
    private Order findOrderForSaga(Integer orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
    }

    private ProductReservationRequestedEvent toProductReservationRequestedEvent(Order order) {
        return new ProductReservationRequestedEvent(
                order.getId(),
                order.getReference(),
                order.getOrderLines().stream()
                        .map(orderLine -> new OrderProductItem(
                                orderLine.getProductId(),
                                orderLine.getQuantity()))
                        .toList());
    }

    private void ensureStatus(Order order, OrderStatus expectedStatus) {
        if (order.getStatus() != expectedStatus) {
            throw new IllegalStateException(
                    "Cannot process saga event for order " + order.getId()
                            + " with status " + order.getStatus()
                            + "; expected " + expectedStatus);
        }
    }

    private RecipientSnapshot toRecipientSnapshot(Order order) {
        return new RecipientSnapshot(
                order.getCustomerId(),
                order.getCustomerFirstName(),
                order.getCustomerLastName(),
                order.getCustomerEmail());
    }

    private List<OrderItemSnapshot> toOrderItemSnapshots(Order order) {
        return order.getOrderLines().stream()
                .map(orderLine -> new OrderItemSnapshot(
                        orderLine.getProductId(),
                        orderLine.getProductName(),
                        orderLine.getQuantity(),
                        orderLine.getUnitPrice(),
                        orderLine.getSubtotal()))
                .toList();
    }
}
