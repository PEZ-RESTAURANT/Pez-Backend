package com.pezbackend.ordering.domain.model.aggregates;

import com.pezbackend.ordering.domain.model.entities.AccountItem;
import com.pezbackend.ordering.domain.model.exceptions.AccountAlreadyClosedException;
import com.pezbackend.ordering.domain.model.exceptions.EmptyAccountException;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import com.pezbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Entity
public class Account extends AuditableAbstractAggregateRoot<Account> {

    @Column(length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    private Long staffId;

    private String customerName;
    private String customerDni;
    private String customerRuc;

    @NotNull
    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountItem> items;

    protected Account() {}

    public Account(Long staffId) {
        this.staffId = staffId;
        this.status = AccountStatus.OPEN;
        this.total = BigDecimal.ZERO;
        this.items = new ArrayList<>();
    }

    // DOMAIN METHODS

    public void addItem(String productName, BigDecimal unitPrice, int quantity, String note) {
        if (this.status == AccountStatus.CLOSED)
            throw new AccountAlreadyClosedException();

        AccountItem item = new AccountItem(this, productName, unitPrice, quantity, note);
        this.items.add(item);
        calculateTotal();
    }

    public void removeItem(Long itemId) {
        if (this.status == AccountStatus.CLOSED)
            throw new AccountAlreadyClosedException();

        Optional<AccountItem> item = this.items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst();

        item.ifPresent(this.items::remove);
        calculateTotal();
    }

    public void updateItemQuantity(Long itemId, int quantity) {
        if (this.status == AccountStatus.CLOSED)
            throw new AccountAlreadyClosedException();

        for (AccountItem item : items) {
            if (item.getId().equals(itemId)) {
                item.updateQuantity(quantity);
                break;
            }
        }
        calculateTotal();
    }

    public void calculateTotal() {
        this.total = items.stream()
                .map(AccountItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void assignCustomer(String name, String dni, String ruc) {
        this.customerName = name;
        this.customerDni = dni;
        this.customerRuc = ruc;
    }

    public void assignName(String name) {
        this.name = name;
    }

    public void closeAccount() {
        if (this.items.isEmpty())
            throw new EmptyAccountException();

        this.status = AccountStatus.CLOSED;
    }
}