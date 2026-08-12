package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al cancelar o eliminar un plato de la comanda.
 * Almacena el mozo original que comandó el plato para fines de auditoría.
 */
public record ItemCancelledEvent(
        Long orderId,
        Long itemId,
        Long productId,
        Integer quantity,
        java.math.BigDecimal unitPriceSnapshot,
        Long originalWaiterId,
        String cancellationReason,
        String detail,
        String cancelledBy,
        LocalDateTime timestamp
) implements DomainEvent {

    public ItemCancelledEvent(Long orderId, Long itemId, Long productId, Integer quantity, java.math.BigDecimal unitPriceSnapshot, Long originalWaiterId, String cancellationReason, String detail, String cancelledBy) {
        this(orderId, itemId, productId, quantity, unitPriceSnapshot, originalWaiterId, cancellationReason, detail, cancelledBy, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ItemCancelled";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return cancelledBy;
    }

    @Override
    public Object payload() {
        return Map.of(
                "orderId", orderId,
                "itemId", itemId,
                "productId", productId,
                "quantity", quantity,
                "unitPriceSnapshot", unitPriceSnapshot,
                "originalWaiterId", originalWaiterId,
                "cancellationReason", cancellationReason,
                "detail", detail != null ? detail : ""
        );
    }
}
