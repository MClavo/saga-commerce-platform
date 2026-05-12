package com.mclavo.ecommerce.order.application;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.customer.CustomerResponse;
import com.mclavo.ecommerce.order.api.OrderRequest;
import com.mclavo.ecommerce.order.api.OrderResponse;
import com.mclavo.ecommerce.order.domain.Order;

@Service
public class OrderMapper {

    public Order toOrder(OrderRequest request, CustomerResponse customer) {
        return new Order(
                request.reference(),
                request.customerId(),
                request.paymentMethod(),
                customer.firstname(),
                customer.lastname(),
                customer.email()
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
