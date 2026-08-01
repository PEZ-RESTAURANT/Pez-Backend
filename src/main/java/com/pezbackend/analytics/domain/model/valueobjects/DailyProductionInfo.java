package com.pezbackend.analytics.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * Record que representa la producción diaria (platos preparados) de un producto.
 */
public record DailyProductionInfo(
        LocalDate date,
        int quantity
) {}
