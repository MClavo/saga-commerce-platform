package com.mclavo.ecommerce.payment.infrastucture.gateway;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.payment.infrastucture.messaging.event.ProductReservationSucceededEvent;

@Service
public class StubPaymentGateway implements PaymentGateway {

    @Override
    public String process(ProductReservationSucceededEvent event) {
        return "PAY-" + event.orderReference();
    }
}
