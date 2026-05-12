package com.mclavo.ecommerce.notification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "payloadType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderConfirmedPayload.class, name = "ORDER_CONFIRMED"),
        @JsonSubTypes.Type(value = PaymentFailedPayload.class, name = "PAYMENT_FAILED"),
        @JsonSubTypes.Type(value = ProductReservationFailedPayload.class, name = "PRODUCT_RESERVATION_FAILED")
})
public sealed interface NotificationPayload permits
        OrderConfirmedPayload,
        PaymentFailedPayload,
        ProductReservationFailedPayload {
}
