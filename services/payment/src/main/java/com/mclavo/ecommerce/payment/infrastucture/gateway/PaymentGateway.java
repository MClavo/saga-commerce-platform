package com.mclavo.ecommerce.payment.infrastucture.gateway;

import com.mclavo.ecommerce.payment.infrastucture.messaging.event.OrderCreatedEvent;

public interface PaymentGateway {
    String process(OrderCreatedEvent event);
}
