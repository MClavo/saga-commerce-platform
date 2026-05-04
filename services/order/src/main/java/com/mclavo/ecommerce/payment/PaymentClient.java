package com.mclavo.ecommerce.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "payment-service",
    path = "/api/v1/payments"
)
public interface PaymentClient {

    @PostMapping
    Integer requestOrderPayment(@RequestBody PaymentRequest request);

}