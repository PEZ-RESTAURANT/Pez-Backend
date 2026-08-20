package com.pezbackend.billing.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento de dominio emitido cuando una venta se marca como ANULADA (VOIDED).
 */
public record SaleVoidedEvent(
        Long saleId,
        Long orderId,
        String voidedReason,
        String voidedBy,
        LocalDateTime timestamp
) implements DomainEvent {

    public SaleVoidedEvent(Long saleId, Long orderId, String voidedReason, String voidedBy) {
        this(saleId, orderId, voidedReason, voidedBy, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "SaleVoidedEvent";
    }

    @Override
    public String module() {
        return "billing";
    }

    @Override
    public String userId() {
        return voidedBy != null ? voidedBy : "system";
    }

    @Override
    public String reason() {
        return voidedReason;
    }

    @Override
    public Object payload() {
        return Map.of(
                "saleId", saleId,
                "orderId", orderId,
                "voidedReason", voidedReason != null ? voidedReason : "",
                "voidedBy", voidedBy != null ? voidedBy : "system"
        );
    }
}
