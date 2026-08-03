package com.pezbackend.orders.domain.services;

import com.pezbackend.orders.domain.model.entities.Reservation;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de consultas (Read) para la gestión de reservas.
 */
public interface ReservationQueryService {
    
    /**
     * Recupera reservas filtrando por fecha y/o estado de forma opcional.
     */
    List<Reservation> getReservations(LocalDate date, String statusStr);

    /**
     * Recupera todas las reservas asociadas a una mesa específica.
     */
    List<Reservation> getTableReservations(Long tableId);

    /**
     * Obtiene una reserva por su ID.
     */
    Reservation getReservationById(Long id);
}
