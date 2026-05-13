package com.mclavo.ecommerce.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mclavo.ecommerce.order.domain.Order;
import com.mclavo.ecommerce.order.domain.OrderProductSnapshot;
import com.mclavo.ecommerce.order.domain.OrderStatus;
import com.mclavo.ecommerce.order.domain.PaymentMethod;

class OrderTest {

    @Test
    void newOrderStartsWithProductReservationPending() {
        Order order = order();

        assertEquals(OrderStatus.PRODUCT_RESERVATION_PENDING, order.getStatus());
    }

    @Test
    void requestedOrderLineDoesNotUpdateTotalAmount() {
        Order order = order();

        order.addRequestedLine(5, 2);

        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        assertEquals(5, order.getOrderLines().getFirst().getProductId());
        assertEquals(2, order.getOrderLines().getFirst().getQuantity());
    }

    @Test
    void productSnapshotsUpdateOrderLinesAndTotalAmount() {
        Order order = order();
        order.addRequestedLine(5, 2);
        order.addRequestedLine(6, 1);

        order.applyProductSnapshots(List.of(
                new OrderProductSnapshot(5, "Claw Hammer", 2, new BigDecimal("18.99")),
                new OrderProductSnapshot(6, "Saw", 1, new BigDecimal("12.50"))));

        assertEquals(new BigDecimal("50.48"), order.getTotalAmount());
        assertEquals("Claw Hammer", order.getOrderLines().getFirst().getProductName());
    }

    @Test
    void pendingCanTransitionToAwaitingPayment() {
        Order order = order();
        order.addRequestedLine(5, 2);
        order.applyProductSnapshots(List.of(
                new OrderProductSnapshot(5, "Claw Hammer", 2, new BigDecimal("18.99"))));

        order.markProductsReserved();

        assertEquals(OrderStatus.AWAITING_PAYMENT, order.getStatus());
    }

    @Test
    void awaitingPaymentCanTransitionToConfirmed() {
        Order order = reservedOrder();

        order.confirm();

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void awaitingPaymentCanTransitionToPaymentFailed() {
        Order order = reservedOrder();

        order.markPaymentFailed();

        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
    }

    @Test
    void pendingCanTransitionToProductReservationFailed() {
        Order order = order();

        order.markProductReservationFailed();

        assertEquals(OrderStatus.PRODUCT_RESERVATION_FAILED, order.getStatus());
    }

    @Test
    void pendingCannotTransitionDirectlyToConfirmed() {
        Order order = order();

        assertThrows(IllegalStateException.class, order::confirm);
    }

    @Test
    void productReservationFailedCannotTransitionToAwaitingPayment() {
        Order order = order();
        order.markProductReservationFailed();

        assertThrows(IllegalStateException.class, order::markProductsReserved);
    }

    private Order order() {
        return new Order(
                "ORD-1",
                "customer-1",
                PaymentMethod.CREDIT_CARD,
                "Ada",
                "Lovelace",
                "ada@example.com");
    }

    private Order reservedOrder() {
        Order order = order();
        order.addRequestedLine(5, 2);
        order.applyProductSnapshots(List.of(
                new OrderProductSnapshot(5, "Claw Hammer", 2, new BigDecimal("18.99"))));
        order.markProductsReserved();
        return order;
    }
}
