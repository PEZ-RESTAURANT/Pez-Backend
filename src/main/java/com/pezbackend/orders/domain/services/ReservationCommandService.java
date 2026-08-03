package com.pezbackend.orders.domain.services;

import com.pezbackend.orders.domain.model.entities.Reservation;
import java.time.LocalDateTime;

/**
 * Servicio de comandos (Write) para la gestión de reservas.
 */
public interface ReservationCommandService {
    
    /**
     * Registra una nueva reserva.
     */
    Reservation createReservation(String customerName, String customerPhone, Long customerId,
                                  LocalDateTime reservationDateTime, Integer partySize, String notes, Long tableId, String waiterUsername);

    /**
     * Actualiza la información y el estado de una reserva existente.
     */
    Reservation updateReservation(Long id, String customerName, String customerPhone, Long customerId,
                                  LocalDateTime reservationDateTime, Integer partySize, String notes, Long tableId, String statusStr, String waiterUsername);

    /**
     * Cancela una reserva cambiándole el estado a CANCELLED.
     */
    void cancelReservation(Long id, String waiterUsername);

    /**
     * Marca una reserva como completada COMPLETED.
     */
    void completeReservation(Long id, String waiterUsername);

    /**
     * Marca una reserva como inasistencia NO_SHOW.
     */
    void noShowReservation(Long id, String waiterUsername);
}
