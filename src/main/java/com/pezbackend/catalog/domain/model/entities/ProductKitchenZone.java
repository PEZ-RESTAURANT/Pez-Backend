package com.pezbackend.catalog.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.infrastructure.TenantContext;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad JPA que representa la relación entre un producto y su zona de cocina asignada.
 */
@Entity
@Table(name = "product_kitchen_zones")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class ProductKitchenZone {

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @PrePersist
    public void prePersist() {
        if (this.restaurantId == null) {
            Long current = TenantContext.getCurrentTenantId();
            this.restaurantId = (current != null) ? current : 1L;
        }
    }

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected ProductKitchenZone() {}

    /**
     * Construye una nueva relación producto-zona de cocina.
     *
     * @param productId ID del producto
     * @param zoneId    ID de la zona de cocina
     */
    public ProductKitchenZone(Long productId, Long zoneId) {
        this.productId = productId;
        this.zoneId = zoneId;
    }
}
