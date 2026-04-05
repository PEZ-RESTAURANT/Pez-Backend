package com.pezbackend.cashregister.interfaces.rest.transform;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.MovementsSummary;
import com.pezbackend.cashregister.interfaces.rest.resources.CashMovementResource;
import com.pezbackend.cashregister.interfaces.rest.resources.CashRegisterResource;

import java.math.BigDecimal;
import java.util.List;

public class CashRegisterResourceAssembler {

    public static CashRegisterResource toResource(CashRegister cashRegister) {

        List<CashMovementResource> movements = cashRegister.getMovements()
                .stream()
                .map(CashMovementResourceAssembler::toResource)
                .toList();

        BigDecimal totalIncome = cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.INCOME)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.EXPENSE)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int countIncome = (int) cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.INCOME)
                .count();

        int countExpense = (int) cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.EXPENSE)
                .count();

        MovementsSummary summary = new MovementsSummary(
                totalIncome,
                totalExpense,
                countExpense,
                countIncome,
                cashRegister.getCurrentBalance()
        );

        return new CashRegisterResource(
                cashRegister.getId(),
                cashRegister.getOpeningBalance(),
                cashRegister.getCurrentBalance(),
                cashRegister.getStatus().name(),
                summary,
                cashRegister.getCreatedAt(),
                cashRegister.getClosedAt(),
                movements
        );
    }
}