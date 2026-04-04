package com.pezbackend.ordering.domain.model.aggregates;

import com.pezbackend.ordering.domain.model.entities.AccountItem;
import com.pezbackend.ordering.domain.model.exceptions.AccountAlreadyClosedException;
import com.pezbackend.ordering.domain.model.exceptions.AccountItemNotFoundException;
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

    @OneToMany(
            mappedBy = "account",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
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
        if (this.status == AccountStatus.PAYMENT_PENDING || this.status == AccountStatus.PAID)
            throw new AccountAlreadyClosedException();

        Optional<AccountItem> existingItem = this.items.stream()
                .filter(i -> i.isSameItem(productName, unitPrice, note))
                .findFirst();

        if (existingItem.isPresent()) {
            AccountItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + quantity);
        } else {
            AccountItem item = new AccountItem(this, productName, unitPrice, quantity, note);
            this.items.add(item);
        }

        // 🔥 Si estaba OPEN pasa a IN_PROGRESS
        if (this.status == AccountStatus.OPEN) {
            this.status = AccountStatus.IN_PROGRESS;
        }

        calculateTotal();
    }

    public void removeItem(Long itemId) {
        if (this.status == AccountStatus.PAYMENT_PENDING)
            throw new AccountAlreadyClosedException();

        AccountItem item = this.items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AccountItemNotFoundException(itemId));

        this.items.remove(item);
        calculateTotal();
    }

    public void updateItemQuantity(Long itemId, int quantity) {
        if (this.status == AccountStatus.PAYMENT_PENDING)
            throw new AccountAlreadyClosedException();

        AccountItem item = this.items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AccountItemNotFoundException(itemId));

        item.updateQuantity(quantity);
        calculateTotal();
    }

    public void increaseItemQuantity(Long itemId) {
        if (this.status == AccountStatus.PAYMENT_PENDING)
            throw new AccountAlreadyClosedException();

        AccountItem item = this.items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AccountItemNotFoundException(itemId));

        item.updateQuantity(item.getQuantity() + 1);
        calculateTotal();
    }

    public void decreaseItemQuantity(Long itemId) {
        if (this.status == AccountStatus.PAYMENT_PENDING)
            throw new AccountAlreadyClosedException();

        AccountItem item = this.items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AccountItemNotFoundException(itemId));

        int newQuantity = item.getQuantity() - 1;

        if (newQuantity <= 0) {
            this.items.remove(item);
        } else {
            item.updateQuantity(newQuantity);
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
        if (this.status == AccountStatus.PAYMENT_PENDING || this.status == AccountStatus.PAID)
            throw new AccountAlreadyClosedException();

        if (this.items.isEmpty())
            throw new EmptyAccountException();

        this.status = AccountStatus.PAYMENT_PENDING;
    }

    public void markAsPaid() {
        if (this.status != AccountStatus.PAYMENT_PENDING)
            throw new IllegalStateException("Account must be payment pending");

        this.status = AccountStatus.PAID;
    }

    public void cancelAccount() {
        if (this.status == AccountStatus.PAID)
            throw new IllegalStateException("Cannot cancel a paid account");

        if (this.status == AccountStatus.CANCELLED)
            return;

        this.status = AccountStatus.CANCELLED;
    }
}