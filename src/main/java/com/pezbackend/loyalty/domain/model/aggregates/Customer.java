package com.pezbackend.loyalty.domain.model.aggregates;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.model.aggregates.AbstractTenantAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Agregado principal que representa a un cliente afiliado al programa de fidelización.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class Customer extends AbstractTenantAggregateRoot<Customer> {

    @NotNull
    @Column(nullable = false, unique = true, length = 50)
    private String phone;

    @NotNull
    @Column(nullable = false, length = 255)
    private String fullName;

    @Column
    private String email;

    @Column
    private LocalDate birthday;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private boolean dataConsentAccepted = false;

    @Column
    private LocalDateTime dataConsentDate;

    @Column(nullable = false)
    private int pointsBalance = 0;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Customer() {}

    /**
     * Construye un nuevo cliente afiliado.
     */
    public Customer(String phone, String fullName, LocalDate birthday, String address, boolean dataConsentAccepted) {
        if (!dataConsentAccepted) {
            throw new BusinessRuleViolationException("DATA_CONSENT_REQUIRED", "El consentimiento de datos es obligatorio para afiliarse al programa de fidelización.");
        }
        this.phone = phone;
        this.fullName = fullName;
        this.birthday = birthday;
        this.address = address;
        this.dataConsentAccepted = true;
        this.dataConsentDate = LocalDateTime.now();
        this.pointsBalance = 0;
    }

    public Customer(String phone, String fullName, String email, LocalDate birthday, String address, boolean dataConsentAccepted) {
        this(phone, fullName, birthday, address, dataConsentAccepted);
        this.email = email;
    }

    /**
     * Acredita puntos acumulados al saldo del cliente.
     *
     * @param points puntos a acumular
     */
    public void earnPoints(int points) {
        if (points < 0) {
            throw new BusinessRuleViolationException("INVALID_POINTS", "No se pueden acumular puntos negativos.");
        }
        this.pointsBalance += points;
    }

    /**
     * Canjea puntos del saldo del cliente.
     *
     * @param points puntos a canjear
     */
    public void redeemPoints(int points) {
        if (points < 0) {
            throw new BusinessRuleViolationException("INVALID_POINTS", "No se pueden canjear puntos negativos.");
        }
        if (this.pointsBalance < points) {
            throw new BusinessRuleViolationException("INSUFFICIENT_POINTS", "El cliente no posee saldo de puntos suficiente para este canje.");
        }
        this.pointsBalance -= points;
    }
}