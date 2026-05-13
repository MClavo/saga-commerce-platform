package com.mclavo.ecommerce.product;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.mclavo.ecommerce.exception.ProductPurchaseException;
import com.mclavo.ecommerce.product.api.ProductStockAdjustmentRequest;
import com.mclavo.ecommerce.product.api.ProductUpdateRequest;
import com.mclavo.ecommerce.product.application.ProductMapper;
import com.mclavo.ecommerce.product.application.ProductService;
import com.mclavo.ecommerce.product.domain.Category;
import com.mclavo.ecommerce.product.domain.Product;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductRepository;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductReservationRepository;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;

@SpringBootTest(classes = {
        ProductService.class,
        ProductMapper.class,
        ProductCatalogSmokeTest.TestConfig.class
})
class ProductCatalogSmokeTest {

    @Resource
    private ProductService productService;

    @Resource
    private ProductRepository productRepository;

    @Resource
    private ProductReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reset((Object) productRepository, (Object) reservationRepository);
    }

    @Test
    void should_Update_Catalog_Fields_And_Preserve_Stock_when_Product_Is_Updated() {

        // given
        Product product = product();
        when(productRepository.findById(5)).thenReturn(Optional.of(product));

        var request = new ProductUpdateRequest(
                "Updated Hammer",
                "Updated description",
                new BigDecimal("24.99"),
                9);

        // when
        var response = productService.updateProduct(5, request);

        // then
        assertAll(
                () -> assertEquals("Updated Hammer", product.getName()),
                () -> assertEquals("Updated description", product.getDescription()),
                () -> assertEquals(new BigDecimal("24.99"), product.getPrice()),
                () -> assertEquals(9, product.getCategory().getId()),
                () -> assertEquals(10, product.getAvailableQuantity()),
                () -> assertEquals(4, product.getReservedQuantity()),
                () -> assertEquals(5, response.id()),
                () -> assertEquals("Updated Hammer", response.name()),
                () -> assertEquals(9, response.categoryId()));
    }

    @Test
    void should_Adjust_Available_Stock_And_Preserve_Reserved_Stock_when_Stock_Adjustment_Is_Applied() {

        // given
        Product product = product();
        when(productRepository.findById(5)).thenReturn(Optional.of(product));

        // when
        var response = productService.adjustStock(5, new ProductStockAdjustmentRequest(-3));

        // then
        assertAll(
                () -> assertEquals(7, product.getAvailableQuantity()),
                () -> assertEquals(4, product.getReservedQuantity()),
                () -> assertEquals(7, response.availableQuantity()));
    }

    @Test
    void should_Throw_when_Stock_Adjustment_Would_Make_Available_Stock_Negative() {

        // given
        Product product = product();
        when(productRepository.findById(5)).thenReturn(Optional.of(product));

        // when / then
        assertThrows(ProductPurchaseException.class,
                () -> productService.adjustStock(5, new ProductStockAdjustmentRequest(-11)));
    }

    @Test
    void should_Throw_when_Product_Does_Not_Exist() {

        // given
        when(productRepository.findById(5)).thenReturn(Optional.empty());

        // when / then
        assertThrows(EntityNotFoundException.class,
                () -> productService.adjustStock(5, new ProductStockAdjustmentRequest(2)));
    }

    private Product product() {
        return Product.builder()
                .id(5)
                .name("Claw Hammer")
                .description("16 oz claw hammer")
                .availableQuantity(10)
                .reservedQuantity(4)
                .price(new BigDecimal("18.99"))
                .category(Category.builder()
                        .id(1)
                        .name("Hand Tools")
                        .description("Hand tools")
                        .build())
                .build();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        ProductRepository productRepository() {
            return mock(ProductRepository.class);
        }

        @Bean
        ProductReservationRepository productReservationRepository() {
            return mock(ProductReservationRepository.class);
        }
    }
}
