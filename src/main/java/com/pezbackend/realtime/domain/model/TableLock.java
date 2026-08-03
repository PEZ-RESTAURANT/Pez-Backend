package com.pezbackend.realtime.domain.model;

import java.time.LocalDateTime;

/**
 * Record en memoria que representa el bloqueo temporal de una mesa por un mozo.
 */
public record TableLock(
        Long tableId,
        Long waiterId,
        String waiterName,
        LocalDateTime lockedAt
) {}
