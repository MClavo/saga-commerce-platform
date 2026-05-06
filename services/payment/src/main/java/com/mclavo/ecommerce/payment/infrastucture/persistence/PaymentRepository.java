package com.mclavo.ecommerce.payment.infrastucture.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mclavo.ecommerce.payment.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

}
