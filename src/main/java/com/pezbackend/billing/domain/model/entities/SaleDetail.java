package com.pezbackend.billing.domain.model.entities;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class SaleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;

    private String productName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private String note;

    protected SaleDetail() {}

    public SaleDetail(Sale sale, String productName, BigDecimal unitPrice, Integer quantity, String note) {
        this.sale = sale;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.note = note;
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}