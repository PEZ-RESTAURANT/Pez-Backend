package com.pezbackend.cashregister.application.internal.commandservices;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.commands.*;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.cashregister.domain.model.exceptions.*;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.queries.GetAccountByIdQuery;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import com.pezbackend.ordering.domain.services.AccountQueryService;
import com.pezbackend.ordering.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CashRegisterCommandServiceImpl implements CashRegisterCommandService {

    private final CashRegisterRepository cashRegisterRepository;
    private final AccountQueryService accountQueryService;

    public CashRegisterCommandServiceImpl(CashRegisterRepository cashRegisterRepository,
                                          AccountQueryService accountQueryService) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.accountQueryService = accountQueryService;
    }

    @Override
    public Long handle(OpenCashRegisterCommand command) {
        // Verificar que no haya caja abierta
        if (cashRegisterRepository.findByStatus(CashRegisterStatus.OPEN).isPresent()) {
            throw new CashRegisterAlreadyOpenException();
        }

        CashRegister cashRegister = new CashRegister(command.openingBalance());
        cashRegisterRepository.save(cashRegister);

        return cashRegister.getId();
    }

    @Override
    public void handle(CloseCashRegisterCommand command) {
        CashRegister cashRegister = cashRegisterRepository
                .findByStatus(CashRegisterStatus.OPEN)
                .orElseThrow(CashRegisterNotOpenException::new);

        cashRegister.close();
        cashRegisterRepository.save(cashRegister);
    }

    @Override
    public Long handle(AddCashMovementCommand command) {

        CashRegister cashRegister = cashRegisterRepository
                .findByStatus(CashRegisterStatus.OPEN)
                .orElseThrow(CashRegisterNotOpenException::new);

        CashMovement movement = new CashMovement(
                command.type(),
                command.amount(),
                command.note()
        );

        cashRegister.addMovement(movement);
        cashRegisterRepository.save(cashRegister);

        return movement.getId();
    }

    @Override
    public Long handle(AddSaleIncomeCommand command) {

        CashRegister cashRegister = cashRegisterRepository
                .findByStatus(CashRegisterStatus.OPEN)
                .orElseThrow(CashRegisterNotOpenException::new);

        CashMovement movement = new CashMovement(
                CashMovementType.INCOME,
                command.amount(),
                command.note()
        );

        cashRegister.addMovement(movement);
        cashRegisterRepository.save(cashRegister);

        return movement.getId();
    }
}