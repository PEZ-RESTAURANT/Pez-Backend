package com.pezbackend.inventory.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando se descuenta stock de un insumo automáticamente por la venta de un plato.
 */
public record SupplyDeductedBySale(
        Long supplyId,
        Long productId,
        BigDecimal quantityDeducted,
        BigDecimal newStock,
        LocalDateTime timestamp
) implements DomainEvent {

    public SupplyDeductedBySale(Long supplyId, Long productId, BigDecimal quantityDeducted, BigDecimal newStock) {
        this(supplyId, productId, quantityDeducted, newStock, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "SupplyDeductedBySale";
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
                "productId", productId,
                "quantityDeducted", quantityDeducted,
                "newStock", newStock
        );
    }
}
