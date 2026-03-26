package com.pezbackend.ordering.domain.model.entities;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class AccountItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String note;

    protected AccountItem() {}

    public AccountItem(Account account, String productName,
                       BigDecimal unitPrice, Integer quantity, String note) {
        this.account = account;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.note = note;
        calculateTotal();
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}