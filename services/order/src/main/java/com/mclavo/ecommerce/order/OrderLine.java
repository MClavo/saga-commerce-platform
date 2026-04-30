package com.mclavo.ecommerce.order;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "customer_line")
class OrderLine {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    private Integer productId;
    private Double quantity;
    private BigDecimal unitPrice;

    // May need to consider adding total price for the line (quantity * unitPrice)
    // for easier querying and reporting
    // But there is no coupon or discount system in place,
    // so there is no need to calculate the line total,
    // it can be calculated on the fly when needed.
    // private BigDecimal lineTotal;
}
