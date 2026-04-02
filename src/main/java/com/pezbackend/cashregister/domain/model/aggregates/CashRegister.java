package com.pezbackend.cashregister.domain.model.aggregates;

import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.exceptions.CashMovementInvalidAmountException;
import com.pezbackend.cashregister.domain.model.exceptions.CashRegisterAlreadyClosedException;
import com.pezbackend.cashregister.domain.model.exceptions.CashRegisterNotOpenException;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import com.pezbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class CashRegister extends AuditableAbstractAggregateRoot<CashRegister> {

    @NotNull
    @Column(nullable = false)
    private BigDecimal openingBalance;

    @NotNull
    @Column(nullable = false)
    private BigDecimal currentBalance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashRegisterStatus status;

    @OneToMany(mappedBy = "cashRegister", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CashMovement> movements = new ArrayList<>();

    protected CashRegister() {}

    // Abrir caja
    public CashRegister(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
        this.currentBalance = openingBalance;
        this.status = CashRegisterStatus.OPEN;
    }

    // Agregar movimiento manual o por venta
    public void addMovement(CashMovement movement) {
        if (status != CashRegisterStatus.OPEN) throw new CashRegisterNotOpenException();
        if (movement.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new CashMovementInvalidAmountException(movement.getAmount());

        movements.add(movement);
        movement.setCashRegister(this);

        if (movement.getType() == CashMovementType.INCOME) {
            currentBalance = currentBalance.add(movement.getAmount());
        } else {
            currentBalance = currentBalance.subtract(movement.getAmount());
        }
    }

    // Cerrar caja
    public void close() {
        if (status == CashRegisterStatus.CLOSED) throw new CashRegisterAlreadyClosedException();
        status = CashRegisterStatus.CLOSED;
    }
}