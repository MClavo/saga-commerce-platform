package com.mclavo.ecommerce.payment.infrastucture.gateway;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.payment.infrastucture.messaging.event.OrderCreatedEvent;

@Service
public class StubPaymentGateway implements PaymentGateway {

    @Override
    public String process(OrderCreatedEvent event) {
        return "PAY-" + event.orderReference();
    }
}
