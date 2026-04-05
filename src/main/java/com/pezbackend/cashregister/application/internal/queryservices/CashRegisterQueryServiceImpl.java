package com.pezbackend.cashregister.application.internal.queryservices;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.queries.*;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import com.pezbackend.cashregister.domain.services.CashRegisterQueryService;
import com.pezbackend.cashregister.domain.model.valueobjects.MovementsSummary;
import com.pezbackend.cashregister.domain.model.exceptions.*;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashRegisterQueryServiceImpl implements CashRegisterQueryService {

    private final CashRegisterRepository cashRegisterRepository;

    public CashRegisterQueryServiceImpl(CashRegisterRepository cashRegisterRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
    }

    @Override
    public CashRegister handle(GetCurrentCashRegisterQuery query) {
        return cashRegisterRepository.findByStatus(CashRegisterStatus.OPEN)
                .orElseThrow(CashRegisterNotOpenException::new);
    }

    @Override
    public CashRegister handle(GetCashRegisterByIdQuery query) {
        return cashRegisterRepository.findById(query.cashRegisterId())
                .orElseThrow(() -> new CashRegisterNotFoundException(query.cashRegisterId()));
    }

    @Override
    public List<CashMovement> handle(GetMovementsByTypeQuery query) {
        CashRegister cashRegister = cashRegisterRepository.findById(query.cashRegisterId())
                .orElseThrow(() -> new CashRegisterNotFoundException(query.cashRegisterId()));

        return cashRegister.getMovements()
                .stream()
                .filter(m -> m.getType() == query.movementType())
                .collect(Collectors.toList());
    }

    @Override
    public MovementsSummary handle(GetMovementsSummaryQuery query) {
        CashRegister cashRegister = cashRegisterRepository.findById(query.cashRegisterId())
                .orElseThrow(() -> new CashRegisterNotFoundException(query.cashRegisterId()));

        if (cashRegister.getMovements().isEmpty()) {
            throw new CashRegisterHasNoMovementsException(cashRegister.getId());
        }

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

        return new MovementsSummary(
                totalIncome,
                totalExpense,
                countExpense,
                countIncome,
                cashRegister.getCurrentBalance()
        );
    }

    @Override
    public List<CashRegister> handle(GetCashRegistersByDateRangeQuery query) {

        if (query.startDate() != null && query.endDate() != null) {
            return cashRegisterRepository
                    .findByCreatedAtBetween(query.startDate(), query.endDate());
        }

        return cashRegisterRepository.findAll();
    }
}