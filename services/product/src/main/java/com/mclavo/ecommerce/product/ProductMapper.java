package com.mclavo.ecommerce.product;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.category.Category;

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

    public ProductPurchaseResponse toProductPurchaseResponse(Product product, Integer quantity) {
            return new ProductPurchaseResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                quantity
            );
    }

}
