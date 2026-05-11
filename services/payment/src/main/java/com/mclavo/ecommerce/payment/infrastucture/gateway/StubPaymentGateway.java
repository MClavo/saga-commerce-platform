package com.mclavo.ecommerce.payment.infrastucture.gateway;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentRequestedEvent;

@Service
public class StubPaymentGateway implements PaymentGateway {

    @Override
    public String process(PaymentRequestedEvent event) {
        return "PAY-" + event.orderReference();
    }
}
