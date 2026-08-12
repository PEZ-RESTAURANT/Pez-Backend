package com.pezbackend.cashregister.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.exceptions.CashMovementInvalidAmountException;
import com.pezbackend.cashregister.domain.model.exceptions.CashMovementTypeMismatchException;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementReason;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;

/**
 * Entidad que representa un movimiento (ingreso o egreso) en una caja registradora.
 */
@Getter
@Setter
@Entity
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class CashMovement extends AbstractTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private CashRegister cashRegister;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CashMovementType type;

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CashMovementReason reason;

    private String note;

    protected CashMovement() {}

    public CashMovement(CashMovementType type, BigDecimal amount, CashMovementReason reason, String note) {

        if (type == null)
            throw new CashMovementTypeMismatchException(null, null);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new CashMovementInvalidAmountException(amount);

        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.note = note;
    }

    public CashMovement(CashMovementType type, BigDecimal amount, String note) {
        this(type, amount, null, note);
    }
}