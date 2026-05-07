package com.mclavo.ecommerce.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.mclavo.ecommerce.order.domain.Order;
import com.mclavo.ecommerce.order.domain.OrderStatus;
import com.mclavo.ecommerce.order.domain.PaymentMethod;


class OrderTest {

    @Test
    void newOrderStartsPendingPayment() {
        Order order = new Order("ORD-1", "customer-1", PaymentMethod.CREDIT_CARD);

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    @Test
    void pendingPaymentCanTransitionToConfirmed() {
        Order order = new Order("ORD-1", "customer-1", PaymentMethod.CREDIT_CARD);

        order.confirmPayment();

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void pendingPaymentCanTransitionToCancelled() {
        Order order = new Order("ORD-1", "customer-1", PaymentMethod.CREDIT_CARD);

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void confirmedOrderCannotBeCancelled() {
        Order order = new Order("ORD-1", "customer-1", PaymentMethod.CREDIT_CARD);
        order.confirmPayment();

        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    void cancelledOrderCannotBeConfirmed() {
        Order order = new Order("ORD-1", "customer-1", PaymentMethod.CREDIT_CARD);
        order.cancel();

        assertThrows(IllegalStateException.class, order::confirmPayment);
    }
}
