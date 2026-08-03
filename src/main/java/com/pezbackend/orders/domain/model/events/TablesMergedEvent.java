package com.pezbackend.orders.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Evento emitido al fusionar mesas en el salón.
 */
public record TablesMergedEvent(
        Long anchorTableId,
        List<Long> mergedTableIds,
        String waiterUsername,
        LocalDateTime timestamp
) implements DomainEvent {

    public TablesMergedEvent(Long anchorTableId, List<Long> mergedTableIds, String waiterUsername) {
        this(anchorTableId, mergedTableIds, waiterUsername, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "TablesMerged";
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
                "mergedTableIds", mergedTableIds
        );
    }
}
