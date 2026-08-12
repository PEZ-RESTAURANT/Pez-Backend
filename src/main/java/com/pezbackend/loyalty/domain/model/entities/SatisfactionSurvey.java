package com.pezbackend.loyalty.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;

/**
 * Entidad JPA que representa una encuesta de satisfacción respondida por un cliente.
 */
@Entity
@Table(name = "satisfaction_surveys")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class SatisfactionSurvey extends AbstractTenantEntity {

    @NotNull
    @Column(nullable = false)
    private Long customerId;

    @Column(length = 255)
    private String favoriteDish;

    @Column(length = 255)
    private String favoriteDrink;

    @NotNull
    @Column(nullable = false)
    private int serviceSatisfaction;

    @NotNull
    @Column(nullable = false)
    private int foodSatisfaction;

    @Column(length = 1000)
    private String suggestion;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected SatisfactionSurvey() {}

    /**
     * Construye una nueva encuesta de satisfacción.
     *
     * @param customerId          id del cliente
     * @param favoriteDish        plato favorito (opcional)
     * @param favoriteDrink       bebida favorita (opcional)
     * @param serviceSatisfaction puntaje del servicio (1-5)
     * @param foodSatisfaction    puntaje de la comida (1-5)
     * @param suggestion          sugerencias opcionales
     * @param date                fecha de la encuesta
     */
    public SatisfactionSurvey(Long customerId, String favoriteDish, String favoriteDrink, int serviceSatisfaction, int foodSatisfaction, String suggestion, LocalDate date) {
        if (serviceSatisfaction < 1 || serviceSatisfaction > 5 || foodSatisfaction < 1 || foodSatisfaction > 5) {
            throw new BusinessRuleViolationException("INVALID_SATISFACTION_SCORE", "La puntuación de satisfacción de servicio y comida debe estar en una escala del 1 al 5.");
        }
        this.customerId = customerId;
        this.favoriteDish = favoriteDish;
        this.favoriteDrink = favoriteDrink;
        this.serviceSatisfaction = serviceSatisfaction;
        this.foodSatisfaction = foodSatisfaction;
        this.suggestion = suggestion;
        this.date = date;
    }
}