package com.mclavo.ecommerce.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.exception.ProductPurchaseException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public Integer createProduct(ProductRequest request) {
        Product product = productMapper.toProduct(request);
        return productRepository.save(product).getId();
    }
    
    public ProductResponse findByID(Integer productId) {
        return productRepository.findById(productId)
        .map(productMapper::toProductResponse)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
    }
    
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
            .map(productMapper::toProductResponse)
            .toList();
    }

    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request) {
        var productIds = request.stream()
            .map(ProductPurchaseRequest::productId)
            .toList();

        // TODO: WHY ORDER BY ID? DOES IT MATTER?, Again why not set instead of list?
        var storedProducts = productRepository.findAllByIdInOrderById(productIds);
        if (storedProducts.size() != productIds.size()) {
            throw new ProductPurchaseException("One or more products not found");
        }
        
        var storedRequest = request.stream()
            .sorted(Comparator.comparing(ProductPurchaseRequest::productId))
            .toList();
        
        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();

        for (int i = 0; i < storedProducts.size(); i++) {
            var storedProduct = storedProducts.get(i);
            var purchaseRequest = storedRequest.get(i);

            if (storedProduct.getAvailableQuantity() < purchaseRequest.quantity()) {
                throw new ProductPurchaseException("Insufficient stock for product with id: " + purchaseRequest.productId());
            }

            storedProduct.setAvailableQuantity(storedProduct.getAvailableQuantity() - purchaseRequest.quantity());
            productRepository.save(storedProduct);

            purchasedProducts.add(productMapper.toProductPurchaseResponse(storedProduct, purchaseRequest.quantity()));
        }

        return purchasedProducts;
    }

    

}
