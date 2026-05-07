package com.mclavo.ecommerce.product.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mclavo.ecommerce.exception.ProductPurchaseException;
import com.mclavo.ecommerce.product.api.ProductPurchaseRequest;
import com.mclavo.ecommerce.product.api.ProductPurchaseResponse;
import com.mclavo.ecommerce.product.api.ProductRequest;
import com.mclavo.ecommerce.product.api.ProductResponse;
import com.mclavo.ecommerce.product.domain.Product;
import com.mclavo.ecommerce.product.domain.ProductReservation;
import com.mclavo.ecommerce.product.domain.ProductReservationStatus;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderCancelledEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderConfirmedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderCreatedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderProductItem;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationSucceededEvent;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductRepository;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductReservationRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductReservationRepository reservationRepository;
    private final ProductMapper productMapper;
    
    @Transactional
    public Integer createProduct(ProductRequest request) {
        Product product = productMapper.toProduct(request);
        return productRepository.save(product).getId();
    }
    
    @Transactional(readOnly = true)
    public ProductResponse findByID(Integer productId) {
        return productRepository.findById(productId)
        .map(productMapper::toProductResponse)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
            .map(productMapper::toProductResponse)
            .toList();
    }

    @Transactional
    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request) {
        var productIds = request.stream()
            .map(ProductPurchaseRequest::productId)
            .toList();

        var storedProducts = productRepository.findAllByIdInOrderByIdForUpdate(productIds);
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

            storedProduct.reserveStock(purchaseRequest.quantity());
            storedProduct.commitReservedStock(purchaseRequest.quantity());
            productRepository.save(storedProduct);

            purchasedProducts.add(productMapper.toProductPurchaseResponse(storedProduct, purchaseRequest.quantity()));
        }

        return purchasedProducts;
    }

    @Transactional
    public ProductReservationSucceededEvent reserveStock(OrderCreatedEvent event) {
        if (reservationRepository.existsByOrderId(event.orderId())) {
            return toReservationSucceededEvent(event);
        }

        var quantitiesByProductId = quantitiesByProductId(event.products());
        var productIds = quantitiesByProductId.keySet().stream()
                .sorted()
                .toList();
        var products = productRepository.findAllByIdInOrderByIdForUpdate(productIds);

        if (products.size() != productIds.size()) {
            throw new ProductPurchaseException("One or more products not found");
        }

        for (Product product : products) {
            Integer quantity = quantitiesByProductId.get(product.getId());
            product.reserveStock(quantity);
            reservationRepository.save(ProductReservation.builder()
                    .orderId(event.orderId())
                    .orderReference(event.orderReference())
                    .productId(product.getId())
                    .quantity(quantity)
                    .status(ProductReservationStatus.RESERVED)
                    .build());
        }

        return toReservationSucceededEvent(event);
    }

    @Transactional
    public void commitReservedStock(OrderConfirmedEvent event) {
        finalizeReservedStock(event.orderId(), StockFinalization.COMMIT);
    }

    @Transactional
    public void releaseReservedStock(OrderCancelledEvent event) {
        finalizeReservedStock(event.orderId(), StockFinalization.RELEASE);
    }

    private Map<Integer, Integer> quantitiesByProductId(List<OrderProductItem> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        OrderProductItem::productId,
                        OrderProductItem::quantity,
                        Integer::sum));
    }

    private ProductReservationSucceededEvent toReservationSucceededEvent(OrderCreatedEvent event) {
        return new ProductReservationSucceededEvent(
                event.orderId(),
                event.orderReference(),
                event.totalAmount(),
                event.paymentMethod());
    }

    private void finalizeReservedStock(Integer orderId, StockFinalization finalization) {
        var reservations = reservationRepository.findByOrderId(orderId).stream()
                .filter(ProductReservation::isReserved)
                .toList();
        if (reservations.isEmpty()) {
            return;
        }

        var reservationsByProductId = reservations.stream()
                .collect(Collectors.toMap(ProductReservation::getProductId, Function.identity()));
        var products = productRepository.findAllByIdInOrderByIdForUpdate(
                reservationsByProductId.keySet().stream().sorted().toList());

        for (Product product : products) {
            ProductReservation reservation = reservationsByProductId.get(product.getId());
            if (finalization == StockFinalization.COMMIT) {
                product.commitReservedStock(reservation.getQuantity());
                reservation.commit();
            } else {
                product.releaseReservedStock(reservation.getQuantity());
                reservation.release();
            }
        }
    }

    private enum StockFinalization {
        COMMIT,
        RELEASE
    }

}
