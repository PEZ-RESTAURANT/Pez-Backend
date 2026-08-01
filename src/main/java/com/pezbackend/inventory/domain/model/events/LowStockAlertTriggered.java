package com.pezbackend.inventory.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando el stock de un insumo desciende por debajo de su umbral mínimo de seguridad.
 */
public record LowStockAlertTriggered(
        Long supplyId,
        String supplyName,
        BigDecimal currentStock,
        BigDecimal threshold,
        LocalDateTime timestamp
) implements DomainEvent {

    public LowStockAlertTriggered(Long supplyId, String supplyName, BigDecimal currentStock, BigDecimal threshold) {
        this(supplyId, supplyName, currentStock, threshold, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "LowStockAlertTriggered";
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
                "currentStock", currentStock,
                "threshold", threshold
        );
    }
}
