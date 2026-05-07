package com.mclavo.ecommerce.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.mclavo.ecommerce.exception.ProductPurchaseException;
import com.mclavo.ecommerce.product.domain.Product;

class ProductTest {

    @Test
    void reserveStock_movesAvailableStockToReservedStock() {
        Product product = Product.builder()
                .id(1)
                .availableQuantity(10)
                .reservedQuantity(0)
                .build();

        product.reserveStock(3);

        assertEquals(7, product.getAvailableQuantity());
        assertEquals(3, product.getReservedQuantity());
    }

    @Test
    void reserveStock_fails_whenAvailableStockIsInsufficient() {
        Product product = Product.builder()
                .id(1)
                .availableQuantity(2)
                .reservedQuantity(0)
                .build();

        assertThrows(ProductPurchaseException.class, () -> product.reserveStock(3));
    }

    @Test
    void commitReservedStock_removesReservedStock() {
        Product product = Product.builder()
                .id(1)
                .availableQuantity(7)
                .reservedQuantity(3)
                .build();

        product.commitReservedStock(3);

        assertEquals(7, product.getAvailableQuantity());
        assertEquals(0, product.getReservedQuantity());
    }

    @Test
    void releaseReservedStock_returnsReservedStockToAvailableStock() {
        Product product = Product.builder()
                .id(1)
                .availableQuantity(7)
                .reservedQuantity(3)
                .build();

        product.releaseReservedStock(3);

        assertEquals(10, product.getAvailableQuantity());
        assertEquals(0, product.getReservedQuantity());
    }
}
