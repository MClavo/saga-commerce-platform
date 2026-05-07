package com.mclavo.ecommerce.order.application;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.order.api.OrderRequest;
import com.mclavo.ecommerce.order.api.OrderResponse;
import com.mclavo.ecommerce.order.domain.Order;

@Service
public class OrderMapper {

    public Order toOrder(OrderRequest request) {
        return new Order(
                request.reference(),
                request.customerId(),
                request.paymentMethod()
        );
    }

    public OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getCustomerId(),
                order.getStatus()
        );
    }

}
