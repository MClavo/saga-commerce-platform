package com.mclavo.ecommerce.payment.infrastucture.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mclavo.ecommerce.payment.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findFirstByOrderIdOrderByIdDesc(Integer orderId);
}
