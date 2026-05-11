package com.mclavo.ecommerce.product;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.mclavo.ecommerce.exception.ProductPurchaseException;
import com.mclavo.ecommerce.product.application.ProductMapper;
import com.mclavo.ecommerce.product.application.ProductService;
import com.mclavo.ecommerce.product.domain.Product;
import com.mclavo.ecommerce.product.domain.ProductReservationStatus;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderProductItem;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationRequestedEvent;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductRepository;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductReservationRepository;

@DataJpaTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ ProductService.class, ProductMapper.class })
@Testcontainers(disabledWithoutDocker = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductServiceStockReservationConcurrencyTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:18.1-alpine"));

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void should_Reserve_Stock_Only_Once_when_Concurrent_Orders_Exceed_Available_Quantity() throws Exception {

        // given
        // A product has 10 units available. Two orders will concurrently try to reserve
        // 7 units each.
        // Only one reservation should be possible because 7 + 7 exceeds the available
        // stock.
        Product product = productRepository.saveAndFlush(Product.builder()
                .name("Concurrent Hammer")
                .availableQuantity(10)
                .reservedQuantity(0)
                .price(new BigDecimal("18.99"))
                .build());

        var firstOrder = productReservationRequestedEvent(1, "ORD-1", product.getId(), 7);
        var secondOrder = productReservationRequestedEvent(2, "ORD-2", product.getId(), 7);

        // The first latch waits until both threads are ready.
        // The second latch releases both threads at almost the same time.
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);

        try {
            // when
            // Both reservation attempts are submitted before being released.
            // This makes the test exercise a real concurrent stock reservation scenario.
            var firstAttempt = executor.submit(() -> reserveAfterStart(ready, start, firstOrder));
            var secondAttempt = executor.submit(() -> reserveAfterStart(ready, start, secondOrder));

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Both reservation calls should be ready before starting");
            start.countDown();

            var attempts = List.of(
                    firstAttempt.get(30, TimeUnit.SECONDS),
                    secondAttempt.get(30, TimeUnit.SECONDS));

            long successfulReservations = attempts.stream()
                    .filter(ReservationAttempt::succeeded)
                    .count();
            long failedReservations = attempts.size() - successfulReservations;

            // then
            // Exactly one reservation must succeed and the other one must fail with the
            // expected domain exception.
            assertEquals(1, successfulReservations);
            assertEquals(1, failedReservations);
            assertInstanceOf(ProductPurchaseException.class, attempts.stream()
                    .filter(attempt -> !attempt.succeeded())
                    .findFirst()
                    .orElseThrow()
                    .failure());

            Product finalProduct = productRepository.findById(product.getId()).orElseThrow();
            var reservations = reservationRepository.findAll();

            // The final database state must match a single successful reservation of 7
            // units.
            assertAll(
                    () -> assertEquals(3, finalProduct.getAvailableQuantity()),
                    () -> assertEquals(7, finalProduct.getReservedQuantity()),
                    () -> assertEquals(1, reservations.size()),
                    () -> assertEquals(1, reservations.stream()
                            .filter(reservation -> reservation.getStatus() == ProductReservationStatus.RESERVED)
                            .count()),
                    () -> assertEquals(7, reservations.get(0).getQuantity()));

        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /**
     * Helper method to perform a reservation attempt after both threads are ready.
     * It waits for the start signal before calling the reservation method,
     * simulating a concurrent scenario.
     * 
     * @param ready Latch to signal when the thread is ready to start.
     * @param start Latch to wait for the signal to start the reservation attempt.
     * @param event Product reservation request to use for the reservation attempt.
     * @return A ReservationAttempt indicating whether the reservation succeeded or
     *         failed with an exception.
     */
    private ReservationAttempt reserveAfterStart(
            CountDownLatch ready,
            CountDownLatch start,
            ProductReservationRequestedEvent event) {

        ready.countDown();
        try {
            if (!start.await(10, TimeUnit.SECONDS)) {
                return ReservationAttempt.failed(new IllegalStateException("Timed out waiting to start"));
            }

            productService.reserveStock(event);
            return ReservationAttempt.success();

        } catch (RuntimeException exception) {
            return ReservationAttempt.failed(exception);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ReservationAttempt.failed(exception);
        }
    }

    private ProductReservationRequestedEvent productReservationRequestedEvent(
            Integer orderId,
            String orderReference,
            Integer productId,
            Integer quantity) {
                 
        return new ProductReservationRequestedEvent(
                orderId,
                orderReference,
                List.of(new OrderProductItem(productId, quantity)));
    }

    private record ReservationAttempt(boolean succeeded, Exception failure) {

        static ReservationAttempt success() {
            return new ReservationAttempt(true, null);
        }

        static ReservationAttempt failed(Exception failure) {
            return new ReservationAttempt(false, failure);
        }
    }
}
