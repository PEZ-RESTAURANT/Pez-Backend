package com.pezbackend.inventory.domain.model.entities;

import org.hibernate.annotations.Filter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Entidad JPA que representa un insumo o materia prima (ej: Pollo, Limones, Pisco).
 */
@Entity
@Table(name = "supplies")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class Supply extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 50)
    private String unit;

    @Column(name = "current_stock", nullable = false, precision = 15, scale = 4)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "min_threshold", precision = 15, scale = 4)
    private BigDecimal minThreshold;

    @Column(name = "critical_threshold", precision = 15, scale = 4)
    private BigDecimal criticalThreshold;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Supply() {}

    /**
     * Construye un nuevo insumo con stock inicial en cero.
     */
    public Supply(String name, String unit, BigDecimal minThreshold) {
        this.name = name;
        this.unit = unit;
        this.currentStock = BigDecimal.ZERO;
        this.minThreshold = minThreshold;
    }

    public Supply(String name, String unit, BigDecimal minThreshold, BigDecimal criticalThreshold) {
        this.name = name;
        this.unit = unit;
        this.currentStock = BigDecimal.ZERO;
        this.minThreshold = minThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    public StockLevel getStockLevel() {
        return getStockLevelFor(this.currentStock);
    }

    public StockLevel getStockLevelFor(BigDecimal stock) {
        if (stock == null) return StockLevel.ESTABLE;
        if (stock.compareTo(BigDecimal.ZERO) <= 0) {
            return StockLevel.AGOTADO;
        }
        BigDecimal crit = getEffectiveCriticalThreshold();
        if (stock.compareTo(crit) <= 0) {
            return StockLevel.CRITICO;
        }
        if (minThreshold != null && stock.compareTo(minThreshold) <= 0) {
            return StockLevel.BAJO;
        }
        return StockLevel.ESTABLE;
    }

    public BigDecimal getEffectiveCriticalThreshold() {
        if (criticalThreshold != null) {
            return criticalThreshold;
        }
        if (minThreshold != null) {
            return minThreshold.multiply(new BigDecimal("0.25")).setScale(4, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}