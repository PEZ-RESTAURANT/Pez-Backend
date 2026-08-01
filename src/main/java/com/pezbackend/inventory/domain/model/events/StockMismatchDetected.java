package com.pezbackend.inventory.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando una deducción de stock resulta en una cantidad insuficiente
 * (lo que genera un stock negativo y evidencia un desajuste o descuadre operativo).
 */
public record StockMismatchDetected(
        Long supplyId,
        String supplyName,
        BigDecimal requiredQuantity,
        BigDecimal availableStock,
        LocalDateTime timestamp
) implements DomainEvent {

    public StockMismatchDetected(Long supplyId, String supplyName, BigDecimal requiredQuantity, BigDecimal availableStock) {
        this(supplyId, supplyName, requiredQuantity, availableStock, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "StockMismatchDetected";
    }

    @Override
    public String module() {
        return "inventory";
    }

    @Override
    public String userId() {
        return "system";
    }

    @Override
    public Object payload() {
        return Map.of(
                "supplyId", supplyId,
                "supplyName", supplyName,
                "requiredQuantity", requiredQuantity,
                "availableStock", availableStock
        );
    }
}
