package com.pezbackend.inventory.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al registrarse un ajuste de stock manual por parte del personal de almacén/administrador.
 */
public record StockAdjustedManually(
        Long supplyId,
        BigDecimal adjustedBy,
        BigDecimal newStock,
        String adjustedByUsername,
        String reason,
        LocalDateTime timestamp
) implements DomainEvent {

    public StockAdjustedManually(Long supplyId, BigDecimal adjustedBy, BigDecimal newStock, String adjustedByUsername, String reason) {
        this(supplyId, adjustedBy, newStock, adjustedByUsername, reason, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "StockAdjustedManually";
    }

    @Override
    public String module() {
        return "inventory";
    }

    @Override
    public String userId() {
        return adjustedByUsername;
    }

    @Override
    public String reason() {
        return reason;
    }

    @Override
    public Object payload() {
        return Map.of(
                "supplyId", supplyId,
                "adjustedBy", adjustedBy,
                "newStock", newStock
        );
    }
}
