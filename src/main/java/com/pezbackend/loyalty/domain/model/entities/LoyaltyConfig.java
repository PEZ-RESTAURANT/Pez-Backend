package com.pezbackend.loyalty.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;

/**
 * Entidad JPA que representa la configuración global del programa de fidelización de clientes.
 */
@Entity
@Table(name = "loyalty_configs")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class LoyaltyConfig extends AbstractTenantEntity {

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minPurchaseAmountForPoints;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal pointsPerCurrencyUnit;

    @NotNull
    @Column(nullable = false)
    private int reviewSatisfactionThreshold = 4;

    @Column(length = 255)
    private String googleReviewUrl;

    @Column(name = "qr_code_image", columnDefinition = "LONGTEXT")
    private String qrCodeImage;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected LoyaltyConfig() {}

    /**
     * Construye una nueva configuración del programa de fidelización.
     *
     * @param minPurchaseAmountForPoints  monto mínimo de compra para acumular puntos
     * @param pointsPerCurrencyUnit       puntos otorgados por unidad monetaria consumida
     * @param reviewSatisfactionThreshold umbral de satisfacción promedio para solicitar reseña (1-5)
     * @param googleReviewUrl             URL para redirigir las reseñas en Google (opcional)
     */
    public LoyaltyConfig(BigDecimal minPurchaseAmountForPoints, BigDecimal pointsPerCurrencyUnit, int reviewSatisfactionThreshold, String googleReviewUrl) {
        this.minPurchaseAmountForPoints = minPurchaseAmountForPoints;
        this.pointsPerCurrencyUnit = pointsPerCurrencyUnit;
        this.reviewSatisfactionThreshold = reviewSatisfactionThreshold;
        this.googleReviewUrl = googleReviewUrl;
    }
}