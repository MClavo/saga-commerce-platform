package com.mclavo.ecommerce.customer;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "customer-service",
    path = "/api/v1/customers"

)
public interface CustomerClient {

    @GetMapping("/{id}")
    Optional<CustomerResponse> findCustomerById(
            @PathVariable("id") String customerId);

}
