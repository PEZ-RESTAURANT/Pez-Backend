package com.pezbackend.analytics.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;

/**
 * Entidad JPA que almacena la configuración de analítica de negocio.
 */
@Entity
@Table(name = "analytics_configs")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class AnalyticsConfig extends AbstractTenantEntity {

    @NotNull
    @Column(nullable = false)
    private Integer lowSalesThresholdUnits = 5;

    @NotNull
    @Column(nullable = false)
    private Integer lowSalesEvaluationPeriodDays = 30;

    @NotNull
    @Column(nullable = false, length = 1000)
    private String datePresets = "{}";

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected AnalyticsConfig() {}

    /**
     * Construye una nueva configuración de analítica.
     *
     * @param lowSalesThresholdUnits       umbral de ventas bajas para alertar productos
     * @param lowSalesEvaluationPeriodDays período de evaluación en días para alertar productos
     * @param datePresets                  presets de fechas predefinidas en formato JSON
     */
    public AnalyticsConfig(Integer lowSalesThresholdUnits, Integer lowSalesEvaluationPeriodDays, String datePresets) {
        this.lowSalesThresholdUnits = lowSalesThresholdUnits;
        this.lowSalesEvaluationPeriodDays = lowSalesEvaluationPeriodDays;
        this.datePresets = datePresets;
    }
}