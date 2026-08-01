package com.pezbackend.analytics.domain.services;

import com.pezbackend.analytics.domain.model.entities.AnalyticsConfig;

/**
 * Interfaz para las operaciones de escritura del módulo de analítica.
 */
public interface AnalyticsCommandService {
    AnalyticsConfig updateConfig(Integer lowSalesThresholdUnits, Integer lowSalesEvaluationPeriodDays, String datePresets);
}
