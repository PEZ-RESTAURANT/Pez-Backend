package com.pezbackend.billing.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento de dominio emitido cuando una venta se marca como PAGADA.
 */
public record SaleMarkedPaid(
        Long saleId,
        Long orderId,
        BigDecimal amount,
        LocalDateTime timestamp
) implements DomainEvent {

    public SaleMarkedPaid(Long saleId, Long orderId, BigDecimal amount) {
        this(saleId, orderId, amount, LocalDateTime.now());
    }

    public SaleMarkedPaid(Long saleId, Long orderId) {
        this(saleId, orderId, BigDecimal.ZERO, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "SaleMarkedPaid";
    }

    @Override
    public String module() {
        return "billing";
    }

    @Override
    public String userId() {
        return "system";
    }

    @Override
    public Object payload() {
        return Map.of(
                "saleId", saleId,
                "orderId", orderId,
                "amount", amount
        );
    }
}
