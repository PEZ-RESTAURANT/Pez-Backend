package com.pezbackend.cashregister.application.internal.commandservices;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.entities.CashRegisterMismatch;
import com.pezbackend.cashregister.domain.model.commands.*;
import com.pezbackend.cashregister.domain.model.events.CashRegisterMatched;
import com.pezbackend.cashregister.domain.model.events.CashRegisterMismatched;
import com.pezbackend.cashregister.domain.model.events.ForcedCloseByCutoff;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.cashregister.domain.model.exceptions.*;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterMismatchRepository;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de comandos de caja registradora.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CashRegisterCommandServiceImpl implements CashRegisterCommandService {

    private final CashRegisterRepository cashRegisterRepository;
    private final CashRegisterMismatchRepository cashRegisterMismatchRepository;
    private final ApplicationEventPublisher eventPublisher;

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
                command.reason(),
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

    @Override
    public void handle(CloseCashRegisterWithDeclarationCommand command) {
        CashRegister cashRegister = cashRegisterRepository.findById(command.cashRegisterId())
                .orElseThrow(() -> new ResourceNotFoundException("CASH_REGISTER_NOT_FOUND", 
                        "No se encontró la caja registradora con ID: " + command.cashRegisterId()));

        if (cashRegister.getStatus() == CashRegisterStatus.CLOSED) {
            throw new CashRegisterAlreadyClosedException();
        }

        BigDecimal expectedAmount = cashRegister.getCurrentBalance();
        BigDecimal declaredAmount = command.declaredAmount();

        // 1. Cerrar caja físicamente (valida movimientos y cambia estado)
        cashRegister.close();
        cashRegisterRepository.save(cashRegister);

        // 2. Determinar estado de descuadre
        String status = "MATCHED";
        LocalDateTime notifiedAdminAt = null;

        if (expectedAmount.compareTo(declaredAmount) != 0) {
            status = "MISMATCHED";
            notifiedAdminAt = LocalDateTime.now();
        }

        // 3. Persistir mismatch
        CashRegisterMismatch mismatch = new CashRegisterMismatch(
                cashRegister.getId(),
                expectedAmount,
                declaredAmount,
                status,
                notifiedAdminAt
        );
        cashRegisterMismatchRepository.save(mismatch);

        // 4. Publicar evento correspondiente
        if (status.equals("MISMATCHED")) {
            eventPublisher.publishEvent(new CashRegisterMismatched(cashRegister.getId(), expectedAmount, declaredAmount, cashRegister.getRestaurantId()));
        } else {
            eventPublisher.publishEvent(new CashRegisterMatched(cashRegister.getId(), expectedAmount, cashRegister.getRestaurantId()));
        }
    }

    @Override
    public void forceCloseCashRegister(Long cashRegisterId) {
        CashRegister cashRegister = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("CASH_REGISTER_NOT_FOUND", 
                        "No se encontró la caja registradora con ID: " + cashRegisterId));

        if (cashRegister.getStatus() == CashRegisterStatus.CLOSED) {
            return;
        }

        // Cierre forzado utilizando el nuevo método de dominio
        cashRegister.forceClose();
        cashRegisterRepository.save(cashRegister);

        // Publicar evento de cierre forzado
        eventPublisher.publishEvent(new ForcedCloseByCutoff(cashRegister.getId(), cashRegister.getClosedAt(), cashRegister.getRestaurantId()));
    }
}