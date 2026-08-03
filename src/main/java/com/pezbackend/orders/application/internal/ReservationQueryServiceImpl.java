package com.pezbackend.orders.application.internal;

import com.pezbackend.orders.domain.model.entities.Reservation;
import com.pezbackend.orders.domain.model.valueobjects.ReservationStatus;
import com.pezbackend.orders.domain.services.ReservationQueryService;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.ReservationRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de consultas para reservas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryServiceImpl implements ReservationQueryService {

    private final ReservationRepository reservationRepository;

    @Override
    public List<Reservation> getReservations(LocalDate date, String statusStr) {
        ReservationStatus status = statusStr != null ? ReservationStatus.valueOf(statusStr.toUpperCase()) : null;
        
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1000);
            if (status != null) {
                return reservationRepository.findAllByReservationDateTimeBetweenAndStatus(start, end, status);
            } else {
                return reservationRepository.findAllByReservationDateTimeBetween(start, end);
            }
        } else {
            if (status != null) {
                return reservationRepository.findAllByStatus(status);
            } else {
                return reservationRepository.findAll();
            }
        }
    }

    @Override
    public List<Reservation> getTableReservations(Long tableId) {
        return reservationRepository.findAllByTableId(tableId);
    }

    @Override
    public Reservation getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reserva no encontrada con ID: " + id));
        Long currentTenant = com.pezbackend.shared.infrastructure.TenantContext.getCurrentTenantId();
        if (currentTenant != null && !currentTenant.equals(reservation.getRestaurantId())) {
            throw new com.pezbackend.shared.domain.exceptions.TenantMismatchException("Reservation", id);
        }
        return reservation;
    }
}
