package com.pezbackend.ordering.domain.model.entities;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.exceptions.InvalidItemPriceException;
import com.pezbackend.ordering.domain.model.exceptions.InvalidItemQuantityException;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;
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

        if (account == null)
            throw new BadRequestException("Account cannot be null");

        if (productName == null || productName.isBlank())
            throw new BadRequestException("Product name cannot be empty");

        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidItemPriceException(unitPrice);

        if (quantity <= 0)
            throw new InvalidItemQuantityException(quantity);

        this.account = account;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.note = note;
        calculateTotal();
    }

    public void updateQuantity(Integer quantity) {
        if (quantity <= 0)
            throw new InvalidItemQuantityException(quantity);

        this.quantity = quantity;
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isSameItem(String productName, BigDecimal unitPrice, String note) {
        boolean sameNote = (this.note == null && note == null) ||
                (this.note != null && this.note.equals(note));

        return this.productName.equals(productName)
                && this.unitPrice.compareTo(unitPrice) == 0
                && sameNote;
    }
}