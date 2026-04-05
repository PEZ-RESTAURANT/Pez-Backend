package com.pezbackend.cashregister.domain.model.entities;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.exceptions.CashMovementInvalidAmountException;
import com.pezbackend.cashregister.domain.model.exceptions.CashMovementTypeMismatchException;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class CashMovement extends AuditableModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private CashRegister cashRegister;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CashMovementType type;

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    private String note;

    protected CashMovement() {}

    public CashMovement(CashMovementType type, BigDecimal amount, String note) {

        if (type == null)
            throw new CashMovementTypeMismatchException(null, null);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new CashMovementInvalidAmountException(amount);

        this.type = type;
        this.amount = amount;
        this.note = note;
    }
}