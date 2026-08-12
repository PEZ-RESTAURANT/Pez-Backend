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

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Supply() {}

    /**
     * Construye un nuevo insumo con stock inicial en cero.
     *
     * @param name         nombre descriptivo del insumo
     * @param unit         unidad de medida (ej: kg, L, un)
     * @param minThreshold umbral mínimo de stock para alertas
     */
    public Supply(String name, String unit, BigDecimal minThreshold) {
        this.name = name;
        this.unit = unit;
        this.currentStock = BigDecimal.ZERO;
        this.minThreshold = minThreshold;
    }
}