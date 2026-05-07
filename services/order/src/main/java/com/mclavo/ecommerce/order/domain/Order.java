package com.mclavo.ecommerce.order.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    public Order(String reference, String customerId, PaymentMethod paymentMethod) {
        this.reference = Objects.requireNonNull(reference, "Reference cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer id cannot be null");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    /**
     * Adds an order line to the order and updates the total amount.
     *
     * @param productId the ID of the product being ordered
     * @param quantity the quantity of the product being ordered
     * @param unitPrice the unit price of the product being ordered
     */
    public void addOrderLine(Integer productId, Integer quantity, BigDecimal unitPrice) {
        OrderLine orderLine = new OrderLine(this, productId, quantity, unitPrice);

        orderLines.add(orderLine);
        totalAmount = totalAmount.add(orderLine.getSubtotal());
    }

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);

    }

    public void confirmPayment() {
        transitionTo(OrderStatus.CONFIRMED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    private void transitionTo(OrderStatus targetStatus) {
        Objects.requireNonNull(targetStatus, "Target status cannot be null");

        if (status == targetStatus) {
            return;
        }

        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Cannot change order status from " + status + " to " + targetStatus);
        }

        status = targetStatus;
    }
}
