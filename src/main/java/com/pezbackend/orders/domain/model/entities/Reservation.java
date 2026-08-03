package com.pezbackend.orders.domain.model.entities;

import com.pezbackend.orders.domain.model.valueobjects.ReservationStatus;
import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una reserva de mesa en el restaurante.
 * Aislada por restaurante (tenant) heredando de AbstractTenantEntity.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation extends AbstractTenantEntity {

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 50)
    private String customerPhone;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "reservation_date_time", nullable = false)
    private LocalDateTime reservationDateTime;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "table_id")
    private Long tableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    /**
     * Constructor requerido por la especificación de JPA.
     */
    protected Reservation() {}

    /**
     * Construye una nueva reserva en estado CONFIRMED.
     */
    public Reservation(String customerName, String customerPhone, Long customerId,
                       LocalDateTime reservationDateTime, Integer partySize, String notes, Long tableId) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerId = customerId;
        this.reservationDateTime = reservationDateTime;
        this.partySize = partySize;
        this.notes = notes;
        this.tableId = tableId;
        this.status = ReservationStatus.CONFIRMED;
    }
}
