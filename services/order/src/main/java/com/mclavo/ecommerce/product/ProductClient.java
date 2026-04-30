package com.mclavo.ecommerce.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private final RestClient restClient;

    ProductClient(@Qualifier("productRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<PurchaseResponse> purchaseProducts(List<PurchaseRequest> requests) {
        return restClient
                .post()
                .uri("/purchase")
                .body(requests)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PurchaseResponse>>() {
                });
    }
}
