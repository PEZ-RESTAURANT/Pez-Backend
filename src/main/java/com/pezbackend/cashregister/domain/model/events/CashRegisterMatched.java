package com.pezbackend.cashregister.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando el arqueo de caja cuadra (MATCHED).
 */
public record CashRegisterMatched(
        Long cashRegisterId,
        BigDecimal amount,
        LocalDateTime timestamp
) implements DomainEvent {

    public CashRegisterMatched(Long cashRegisterId, BigDecimal amount) {
        this(cashRegisterId, amount, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "CashRegisterMatched";
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
                "amount", amount
        );
    }
}
