package com.pezbackend.billing.domain.model.entities;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class SalePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;

    protected SalePayment() {}

    public SalePayment(Sale sale, PaymentMethod method, BigDecimal amount) {
        this.sale = sale;
        this.method = method;
        this.amount = amount;
    }
}