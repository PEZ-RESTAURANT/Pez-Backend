package com.pezbackend.cashregister.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando el arqueo de caja resulta en descuadre (MISMATCHED).
 */
public record CashRegisterMismatched(
        Long cashRegisterId,
        BigDecimal expectedAmount,
        BigDecimal declaredAmount,
        LocalDateTime timestamp
) implements DomainEvent {

    public CashRegisterMismatched(Long cashRegisterId, BigDecimal expectedAmount, BigDecimal declaredAmount) {
        this(cashRegisterId, expectedAmount, declaredAmount, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "CashRegisterMismatched";
    }

    @Override
    public String module() {
        return "cashregister";
    }

    @Override
    public String userId() {
        return "system";
    }

    @Override
    public Object payload() {
        return Map.of(
                "cashRegisterId", cashRegisterId,
                "expectedAmount", expectedAmount,
                "declaredAmount", declaredAmount
        );
    }
}
