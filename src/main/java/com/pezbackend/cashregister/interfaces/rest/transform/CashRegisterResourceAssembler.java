package com.pezbackend.cashregister.interfaces.rest.transform;

import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.MovementsSummary;
import com.pezbackend.cashregister.interfaces.rest.resources.CashMovementResource;
import com.pezbackend.cashregister.interfaces.rest.resources.CashRegisterResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CashRegisterResourceAssembler {

    private final SaleRepository saleRepository;

    public CashRegisterResourceAssembler(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public CashRegisterResource toResource(CashRegister cashRegister) {

        List<CashMovementResource> movements = cashRegister.getMovements()
                .stream()
                .map(CashMovementResourceAssembler::toResource)
                .toList();

        // Calculate totalSales: sum of PAID sales in the shift time range
        LocalDateTime end = cashRegister.getClosedAt() != null ? cashRegister.getClosedAt() : LocalDateTime.now();
        BigDecimal totalSales = saleRepository.findByCreatedAtBetween(cashRegister.getCreatedAt(), end).stream()
                .filter(s -> s.getSaleStatus() == com.pezbackend.billing.domain.model.valueobjects.SaleStatus.PAID)
                .map(com.pezbackend.billing.domain.model.aggregates.Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate manual incomes (reason is not null)
        BigDecimal totalManualIncome = cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.INCOME && m.getReason() != null)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate manual expenses (reason is not null)
        BigDecimal totalManualExpense = cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.EXPENSE && m.getReason() != null)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int countIncome = (int) cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.INCOME)
                .count();

        int countExpense = (int) cashRegister.getMovements().stream()
                .filter(m -> m.getType() == CashMovementType.EXPENSE)
                .count();

        MovementsSummary summary = new MovementsSummary(
                totalSales,
                totalManualIncome,
                totalManualExpense,
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