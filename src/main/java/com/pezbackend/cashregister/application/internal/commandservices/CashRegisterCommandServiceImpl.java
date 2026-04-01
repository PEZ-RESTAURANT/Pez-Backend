package com.pezbackend.cashregister.application.internal.commandservices;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.commands.*;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.cashregister.domain.model.exceptions.*;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CashRegisterCommandServiceImpl implements CashRegisterCommandService {

    private final CashRegisterRepository cashRegisterRepository;

    public CashRegisterCommandServiceImpl(CashRegisterRepository cashRegisterRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
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
        CashRegister cashRegister = cashRegisterRepository.findById(command.cashRegisterId())
                .orElseThrow(() -> new CashRegisterNotFoundException(command.cashRegisterId()));

        if (cashRegister.getStatus() != CashRegisterStatus.OPEN) {
            throw new CashRegisterAlreadyClosedException();
        }

        cashRegister.close();
        cashRegisterRepository.save(cashRegister);
    }

    @Override
    public Long handle(AddCashMovementCommand command) {
        CashRegister cashRegister = cashRegisterRepository.findById(command.cashRegisterId())
                .orElseThrow(() -> new CashRegisterNotFoundException(command.cashRegisterId()));

        if (cashRegister.getStatus() != CashRegisterStatus.OPEN) {
            throw new CashRegisterNotOpenException();
        }

        if (command.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new CashMovementInvalidAmountException(command.amount());
        }

        CashMovement movement = new CashMovement(command.type(), command.amount(), command.note());
        cashRegister.addMovement(movement);

        cashRegisterRepository.save(cashRegister);

        return movement.getId();
    }

    @Override
    public Long handle(AddSaleIncomeCommand command) {
        CashRegister cashRegister = cashRegisterRepository.findById(command.cashRegisterId())
                .orElseThrow(() -> new CashRegisterNotFoundException(command.cashRegisterId()));

        if (cashRegister.getStatus() != CashRegisterStatus.OPEN) {
            throw new CashRegisterNotOpenException();
        }

        String note = "Ingreso por venta: " + command.accountName() + " (ID " + command.accountId() + ")";
        CashMovement movement = new CashMovement(CashMovementType.INCOME, command.amount(), note);

        cashRegister.addMovement(movement);
        cashRegisterRepository.save(cashRegister);

        return movement.getId();
    }
}