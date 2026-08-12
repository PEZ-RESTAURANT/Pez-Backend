package com.pezbackend.tenancy.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad JPA que almacena la configuración operativa general del restaurante.
 */
@Entity
@Table(name = "operational_configs")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class OperationalConfig extends AbstractTenantEntity {

    @NotNull
    @Column(nullable = false)
    private Integer cutoffHour = 3;

    @NotNull
    @Column(nullable = false)
    private Integer cutoffMinute = 0;

    @NotNull
    @Column(nullable = false)
    private Integer unattendedThresholdMinutes = 15;

    @NotNull
    @Column(nullable = false)
    private Integer waitingDishesThresholdMinutes = 30;

    protected OperationalConfig() {}

    public OperationalConfig(Integer cutoffHour, Integer cutoffMinute, Integer unattendedThresholdMinutes, Integer waitingDishesThresholdMinutes) {
        this.cutoffHour = cutoffHour;
        this.cutoffMinute = cutoffMinute;
        this.unattendedThresholdMinutes = unattendedThresholdMinutes;
        this.waitingDishesThresholdMinutes = waitingDishesThresholdMinutes;
    }
}