package com.pezbackend.orders.infrastructure.persistence.jpa.repositories;

import com.pezbackend.orders.domain.model.entities.Reservation;
import com.pezbackend.orders.domain.model.valueobjects.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para gestionar la persistencia de las reservas.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Busca las reservas cuya fecha y hora se encuentren en el rango y con un estado específico.
     */
    List<Reservation> findAllByReservationDateTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, ReservationStatus status);

    /**
     * Busca las reservas cuya fecha y hora se encuentren en el rango.
     */
    List<Reservation> findAllByReservationDateTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Busca las reservas con un estado específico.
     */
    List<Reservation> findAllByStatus(ReservationStatus status);

    /**
     * Busca todas las reservas asociadas a una mesa física.
     */
    List<Reservation> findAllByTableId(Long tableId);
}
