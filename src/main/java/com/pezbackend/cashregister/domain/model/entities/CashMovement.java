package com.pezbackend.cashregister.domain.model.entities;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
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

    public CashMovement(CashMovementType cashMovementType, BigDecimal amount, String note) {
        this.type = cashMovementType;
        this.amount = amount;
        this.note = note;
    }
}