package com.mclavo.ecommerce.product.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mclavo.ecommerce.product.application.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Integer> createProduct(
        @RequestBody @Valid ProductRequest request
    ) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Integer id,
        @RequestBody @Valid ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PostMapping("/{id}/stock-adjustments")
    public ResponseEntity<ProductResponse> adjustStock(
        @PathVariable Integer id,
        @RequestBody @Valid ProductStockAdjustmentRequest request
    ) {
        return ResponseEntity.ok(productService.adjustStock(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
        @PathVariable Integer id
    ) {
        return ResponseEntity.ok(productService.findByID(id));
    }

    // TODO:   Pagination ??? 
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll(){
        return ResponseEntity.ok(productService.findAll());
    }
    
}
