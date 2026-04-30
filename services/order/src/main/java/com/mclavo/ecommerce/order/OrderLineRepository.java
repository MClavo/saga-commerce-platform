package com.mclavo.ecommerce.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderLineRepository extends JpaRepository<OrderLine, Integer> {

    @Query("""
        SELECT new com.mclavo.ecommerce.order.OrderLineResponse(
            ol.id,
            ol.productId,
            ol.quantity,
            ol.unitPrice
        )
        FROM OrderLine ol
        WHERE ol.order.id = :orderId
        ORDER BY ol.id
    """)
    List<OrderLineResponse> findAllByOrderId(
            @Param("orderId") Integer orderId
    );
}
