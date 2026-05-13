package com.mclavo.ecommerce.order.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "customer_order")
public class Order {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PRODUCT_RESERVATION_PENDING;

    @Column(nullable = false)
    private String customerFirstName;

    @Column(nullable = false)
    private String customerLastName;

    @Column(nullable = false)
    private String customerEmail;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    public Order(
            String reference,
            String customerId,
            PaymentMethod paymentMethod,
            String customerFirstName,
            String customerLastName,
            String customerEmail) {
        this.reference = Objects.requireNonNull(reference, "Reference cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer id cannot be null");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        this.customerFirstName = Objects.requireNonNull(customerFirstName, "Customer first name cannot be null");
        this.customerLastName = Objects.requireNonNull(customerLastName, "Customer last name cannot be null");
        this.customerEmail = Objects.requireNonNull(customerEmail, "Customer email cannot be null");
        this.status = OrderStatus.PRODUCT_RESERVATION_PENDING;
    }

    /**
     * Records the customer's requested product and quantity before stock reservation.
     * Product name, unit price, subtotal, and total remain unresolved until Product
     * Service returns confirmed snapshots.
     */
    public void addRequestedLine(Integer productId, Integer quantity) {
        orderLines.add(new OrderLine(this, productId, quantity));
    }

    /**
     * Applies all confirmed product snapshots atomically from the aggregate's point
     * of view and recalculates the order total once.
     *
     * <p>The snapshot set must exactly match the requested lines. This prevents the
     * Order aggregate from paying for products that were not requested, or from
     * finalizing an order with missing commercial data.</p>
     */
    public void applyProductSnapshots(List<OrderProductSnapshot> snapshots) {
        Objects.requireNonNull(snapshots, "Product snapshots cannot be null");

        Map<Integer, OrderLine> linesByProductId = linesByProductId();
        Map<Integer, OrderProductSnapshot> snapshotsByProductId = snapshotsByProductId(snapshots);
        if (!snapshotsByProductId.keySet().equals(linesByProductId.keySet())) {
            throw new IllegalStateException("Product snapshots do not match requested order lines");
        }

        for (OrderProductSnapshot snapshot : snapshotsByProductId.values()) {
            OrderLine orderLine = linesByProductId.get(snapshot.productId());
            orderLine.applySnapshot(snapshot);
        }

        recalculateTotalAmount();
    }

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);

    }

    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED, OrderStatus.AWAITING_PAYMENT);
    }

    /**
     * Moves the saga to the payment step only after every order line has confirmed
     * Product Service data. These snapshots are the source of truth for payment.
     */
    public void markProductsReserved() {
        if (orderLines.isEmpty() || orderLines.stream().anyMatch(line -> !line.hasProductSnapshot())) {
            throw new IllegalStateException("Cannot mark products reserved before product snapshots are applied");
        }
        transitionTo(OrderStatus.AWAITING_PAYMENT, OrderStatus.PRODUCT_RESERVATION_PENDING);
    }

    /**
     * Product reservation failures stop the order flow before payment starts.
     */
    public void markProductReservationFailed() {
        transitionTo(OrderStatus.PRODUCT_RESERVATION_FAILED, OrderStatus.PRODUCT_RESERVATION_PENDING);
    }

    /**
     * Payment failures keep the internal failure reason explicit while Order Service
     * still publishes an order.cancelled integration event for stock compensation.
     */
    public void markPaymentFailed() {
        transitionTo(OrderStatus.PAYMENT_FAILED, OrderStatus.AWAITING_PAYMENT);
    }

    private void transitionTo(OrderStatus targetStatus, OrderStatus expectedCurrentStatus) {
        Objects.requireNonNull(targetStatus, "Target status cannot be null");
        Objects.requireNonNull(expectedCurrentStatus, "Expected current status cannot be null");

        if (status == targetStatus) {
            return;
        }

        if (status != expectedCurrentStatus) {
            throw new IllegalStateException(
                    "Cannot change order status from " + status + " to " + targetStatus);
        }

        status = targetStatus;
    }

    private void recalculateTotalAmount() {
        totalAmount = orderLines.stream()
                .map(OrderLine::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Integer, OrderLine> linesByProductId() {
        Map<Integer, OrderLine> linesByProductId = new LinkedHashMap<>();
        for (OrderLine line : orderLines) {
            OrderLine duplicate = linesByProductId.put(line.getProductId(), line);
            if (duplicate != null) {
                throw new IllegalStateException("Order contains duplicate product line: " + line.getProductId());
            }
        }
        return linesByProductId;
    }

    private Map<Integer, OrderProductSnapshot> snapshotsByProductId(List<OrderProductSnapshot> snapshots) {
        Map<Integer, OrderProductSnapshot> snapshotsByProductId = new LinkedHashMap<>();
        for (OrderProductSnapshot snapshot : snapshots) {
            OrderProductSnapshot duplicate = snapshotsByProductId.put(snapshot.productId(), snapshot);
            if (duplicate != null) {
                throw new IllegalStateException("Product snapshots contain duplicate product: " + snapshot.productId());
            }
        }
        return snapshotsByProductId;
    }
}
