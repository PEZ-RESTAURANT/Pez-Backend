package com.pezbackend.analytics.interfaces.rest.resources;

/**
 * Recurso DTO para transferir y exponer los datos de configuración de analítica de negocio.
 */
public record AnalyticsConfigResource(
        Integer lowSalesThresholdUnits,
        Integer lowSalesEvaluationPeriodDays,
        String datePresets
) {}
