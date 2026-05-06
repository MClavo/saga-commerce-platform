package com.mclavo.ecommerce.payment.infrastucture.gateway;

import com.mclavo.ecommerce.payment.infrastucture.messaging.event.ProductReservationSucceededEvent;

public interface PaymentGateway {
    String process(ProductReservationSucceededEvent event);
}
