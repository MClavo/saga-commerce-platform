package com.mclavo.ecommerce.product;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
    name = "product-service",
    path = "/api/v1/products"
)
public interface ProductClient {


    @PostMapping("/purchase")
    public List<PurchaseResponse> purchaseProducts(List<PurchaseRequest> requests);

}
