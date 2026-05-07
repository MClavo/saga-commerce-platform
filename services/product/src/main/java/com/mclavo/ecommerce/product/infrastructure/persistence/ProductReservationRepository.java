package com.mclavo.ecommerce.product.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mclavo.ecommerce.product.domain.ProductReservation;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, Integer> {
    boolean existsByOrderId(Integer orderId);

    List<ProductReservation> findByOrderId(Integer orderId);
}
