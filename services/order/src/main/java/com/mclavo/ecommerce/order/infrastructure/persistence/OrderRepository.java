package com.mclavo.ecommerce.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mclavo.ecommerce.order.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

}
