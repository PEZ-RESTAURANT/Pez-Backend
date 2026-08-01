package com.pezbackend.cashregister.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido cuando una caja se cierra automáticamente por corte de horario.
 */
public record ForcedCloseByCutoff(
        Long cashRegisterId,
        LocalDateTime closedAt,
        LocalDateTime timestamp
) implements DomainEvent {

    public ForcedCloseByCutoff(Long cashRegisterId, LocalDateTime closedAt) {
        this(cashRegisterId, closedAt, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ForcedCloseByCutoff";
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
                "closedAt", closedAt
        );
    }
}
