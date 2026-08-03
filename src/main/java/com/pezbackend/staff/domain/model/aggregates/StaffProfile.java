package com.pezbackend.staff.domain.model.aggregates;

import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.shared.domain.model.aggregates.AbstractTenantAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agregado principal que representa el perfil y contrato laboral de un empleado en el sistema.
 */
@Entity
@Table(name = "staff_profiles")
@Getter
@Setter
public class StaffProfile extends AbstractTenantAggregateRoot<StaffProfile> {

    @NotNull
    @Column(nullable = false, unique = true)
    private Long accountId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StaffPaymentType paymentType;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal agreedAmount;

    @Column(nullable = false)
    private boolean fingerprintConsent = false;

    @Column
    private LocalDateTime fingerprintConsentDate;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected StaffProfile() {}

    /**
     * Construye un nuevo perfil de personal.
     *
     * @param accountId   id de la cuenta de usuario (iam.User) asociada
     * @param paymentType modalidad de pago acordada
     * @param agreedAmount monto acordado para la modalidad de pago
     */
    public StaffProfile(Long accountId, StaffPaymentType paymentType, BigDecimal agreedAmount) {
        this.accountId = accountId;
        this.paymentType = paymentType;
        this.agreedAmount = agreedAmount;
        this.fingerprintConsent = false;
    }

    /**
     * Actualiza la información laboral del empleado.
     *
     * @param paymentType modalidad de pago acordada
     * @param agreedAmount monto acordado para la modalidad de pago
     */
    public void updateProfile(StaffPaymentType paymentType, BigDecimal agreedAmount) {
        this.paymentType = paymentType;
        this.agreedAmount = agreedAmount;
    }

    /**
     * Registra o revoca el consentimiento de huella dactilar.
     *
     * @param consent valor booleano indicando el estado del consentimiento
     */
    public void recordFingerprintConsent(boolean consent) {
        this.fingerprintConsent = consent;
        this.fingerprintConsentDate = consent ? LocalDateTime.now() : null;
    }
}
