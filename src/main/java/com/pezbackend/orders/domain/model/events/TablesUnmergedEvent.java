package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Evento emitido al deshacer la fusión de mesas en el salón.
 */
public record TablesUnmergedEvent(
        Long anchorTableId,
        List<Long> unmergedTableIds,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public TablesUnmergedEvent(Long anchorTableId, List<Long> unmergedTableIds, String waiterUsername) {
        this(anchorTableId, unmergedTableIds, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "TablesUnmerged";
    }

    @Override
    public String module() {
        return "orders";
    }

    @Override
    public String userId() {
        return waiterUsername;
    }

    @Override
    public Object payload() {
        return Map.of(
                "anchorTableId", anchorTableId,
                "unmergedTableIds", unmergedTableIds
        );
    }
}
