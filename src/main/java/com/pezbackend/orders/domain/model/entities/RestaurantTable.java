package com.pezbackend.orders.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.orders.domain.model.valueobjects.TableStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.pezbackend.shared.domain.model.entities.AbstractTenantBaseEntity;

/**
 * Representa una mesa física dentro del salón del restaurante.
 */
@Entity
@Table(name = "restaurant_tables", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurant_id", "number"})
})
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class RestaurantTable extends AbstractTenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer floor;

    @Column(name = "zone_tag", length = 100)
    private String zoneTag;

    @Column(name = "position_x", nullable = false)
    private Integer positionX;

    @Column(name = "position_y", nullable = false)
    private Integer positionY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TableStatus status = TableStatus.FREE;

    @Column(name = "anchor_table_id")
    private Long anchorTableId;

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected RestaurantTable() {}

    /**
     * Construye una nueva mesa con sus coordenadas y estado inicial FREE.
     *
     * @param number    número único de la mesa
     * @param floor     piso en el que se ubica
     * @param zoneTag   etiqueta de la zona (ej: "Terraza", "Vip")
     * @param positionX coordenada horizontal para el layout visual
     * @param positionY coordenada vertical para el layout visual
     */
    public RestaurantTable(Integer number, Integer floor, String zoneTag, Integer positionX, Integer positionY) {
        this.number = number;
        this.floor = floor;
        this.zoneTag = zoneTag;
        this.positionX = positionX;
        this.positionY = positionY;
        this.status = TableStatus.FREE;
    }
}