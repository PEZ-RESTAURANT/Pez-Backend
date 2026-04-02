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
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import com.pezbackend.ordering.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CashRegisterCommandServiceImpl implements CashRegisterCommandService {

    private final CashRegisterRepository cashRegisterRepository;
    private final AccountRepository accountRepository;

    public CashRegisterCommandServiceImpl(CashRegisterRepository cashRegisterRepository,
                                          AccountRepository accountRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.accountRepository = accountRepository;
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

        // 🔍 Obtener caja abierta
        CashRegister cashRegister = cashRegisterRepository.findByStatus(CashRegisterStatus.OPEN)
                .orElseThrow(CashRegisterNotOpenException::new);

        // 🔍 Obtener cuenta
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // 🧠 Validación
        if (!account.getStatus().equals(AccountStatus.PAYMENT_PENDING)) {
            throw new IllegalStateException("Account must be closed to register income");
        }

        // 💰 Crear movimiento automáticamente
        String note = "Ingreso por venta: " + account.getName() + " (ID " + account.getId() + ")";

        CashMovement movement = new CashMovement(
                CashMovementType.INCOME,
                account.getTotal(),
                note
        );

        cashRegister.addMovement(movement);
        cashRegisterRepository.save(cashRegister);

        return movement.getId();
    }
}