package com.pezbackend.staff.domain.model.aggregates;

import org.hibernate.annotations.Filter;
import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.shared.domain.model.aggregates.AbstractTenantAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Agregado principal que representa el perfil y contrato laboral de un empleado en el sistema.
 */
@Entity
@Table(name = "staff_profiles")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
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

    @Column(name = "overtime_hourly_rate", precision = 19, scale = 4)
    private BigDecimal overtimeHourlyRate;

    @Column(nullable = false)
    private boolean fingerprintConsent = false;

    @Column
    private LocalDateTime fingerprintConsentDate;

    @Column(name = "fingerprint_id")
    private Integer fingerprintId;

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
        this.overtimeHourlyRate = calculateDefaultOvertimeRate(paymentType, agreedAmount);
        this.fingerprintConsent = false;
    }

    public StaffProfile(Long accountId, StaffPaymentType paymentType, BigDecimal agreedAmount, BigDecimal overtimeHourlyRate) {
        this.accountId = accountId;
        this.paymentType = paymentType;
        this.agreedAmount = agreedAmount;
        this.overtimeHourlyRate = overtimeHourlyRate != null ? overtimeHourlyRate : calculateDefaultOvertimeRate(paymentType, agreedAmount);
        this.fingerprintConsent = false;
    }

    public BigDecimal getOvertimeHourlyRate() {
        if (overtimeHourlyRate == null) {
            return calculateDefaultOvertimeRate(paymentType, agreedAmount);
        }
        return overtimeHourlyRate;
    }

    private BigDecimal calculateDefaultOvertimeRate(StaffPaymentType paymentType, BigDecimal agreedAmount) {
        if (agreedAmount == null || paymentType == null) return BigDecimal.ZERO;
        return switch (paymentType) {
            case HOURLY -> agreedAmount;
            case DAILY -> agreedAmount.divide(BigDecimal.valueOf(8), 4, RoundingMode.HALF_UP);
            case BIWEEKLY -> agreedAmount.divide(BigDecimal.valueOf(120), 4, RoundingMode.HALF_UP);
            case MONTHLY -> agreedAmount.divide(BigDecimal.valueOf(240), 4, RoundingMode.HALF_UP);
        };
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
        this.overtimeHourlyRate = calculateDefaultOvertimeRate(paymentType, agreedAmount);
    }

    public void updateProfile(StaffPaymentType paymentType, BigDecimal agreedAmount, BigDecimal overtimeHourlyRate) {
        this.paymentType = paymentType;
        this.agreedAmount = agreedAmount;
        this.overtimeHourlyRate = overtimeHourlyRate != null ? overtimeHourlyRate : calculateDefaultOvertimeRate(paymentType, agreedAmount);
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