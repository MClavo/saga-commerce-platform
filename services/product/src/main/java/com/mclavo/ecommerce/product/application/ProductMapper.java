package com.mclavo.ecommerce.product.application;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.product.api.ProductRequest;
import com.mclavo.ecommerce.product.api.ProductResponse;
import com.mclavo.ecommerce.product.domain.Category;
import com.mclavo.ecommerce.product.domain.Product;

@Service
public class ProductMapper {

    public Product toProduct(ProductRequest request) {
        Category category = Category.builder()
            .id(request.categoryId())
            .build();
        
        return Product.builder()
            .id(request.id())
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .availableQuantity(request.availableQuantity())
            .reservedQuantity(0)
            .category(category)
            .build();

    }

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getAvailableQuantity(),
            product.getPrice(),
            product.getCategory().getId(),
            product.getCategory().getName(),
            product.getCategory().getDescription()
        );
    }

}
