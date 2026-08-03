package com.pezbackend.orders.application.internal;

import com.pezbackend.orders.domain.model.entities.Reservation;
import com.pezbackend.orders.domain.model.events.ReservationCancelledEvent;
import com.pezbackend.orders.domain.model.events.ReservationCompletedEvent;
import com.pezbackend.orders.domain.model.events.ReservationCreatedEvent;
import com.pezbackend.orders.domain.model.events.ReservationNoShowEvent;
import com.pezbackend.orders.domain.model.valueobjects.ReservationStatus;
import com.pezbackend.orders.domain.services.ReservationCommandService;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.ReservationRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de comandos para la gestión de reservas.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Reservation createReservation(String customerName, String customerPhone, Long customerId,
                                         LocalDateTime reservationDateTime, Integer partySize, String notes, Long tableId, String waiterUsername) {
        Reservation reservation = new Reservation(customerName, customerPhone, customerId, reservationDateTime, partySize, notes, tableId);
        reservation = reservationRepository.save(reservation);
        log.info("Reserva creada con ID: {} para cliente: {}.", reservation.getId(), customerName);
        eventPublisher.publishEvent(new ReservationCreatedEvent(reservation.getId(), customerName, waiterUsername));
        return reservation;
    }

    @Override
    public Reservation updateReservation(Long id, String customerName, String customerPhone, Long customerId,
                                         LocalDateTime reservationDateTime, Integer partySize, String notes, Long tableId, String statusStr, String waiterUsername) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reserva no encontrada con ID: " + id));

        verifyTenant(reservation);

        reservation.setCustomerName(customerName);
        reservation.setCustomerPhone(customerPhone);
        reservation.setCustomerId(customerId);
        reservation.setReservationDateTime(reservationDateTime);
        reservation.setPartySize(partySize);
        reservation.setNotes(notes);
        reservation.setTableId(tableId);

        if (statusStr != null) {
            reservation.setStatus(ReservationStatus.valueOf(statusStr.toUpperCase()));
        }

        reservation = reservationRepository.save(reservation);
        log.info("Reserva con ID: {} actualizada por: {}.", id, waiterUsername);
        return reservation;
    }

    @Override
    public void cancelReservation(Long id, String waiterUsername) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reserva no encontrada con ID: " + id));
        verifyTenant(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        log.info("Reserva con ID: {} cancelada por: {}.", id, waiterUsername);
        eventPublisher.publishEvent(new ReservationCancelledEvent(id, waiterUsername));
    }

    @Override
    public void completeReservation(Long id, String waiterUsername) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reserva no encontrada con ID: " + id));
        verifyTenant(reservation);
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
        log.info("Reserva con ID: {} completada por: {}.", id, waiterUsername);
        eventPublisher.publishEvent(new ReservationCompletedEvent(id, waiterUsername));
    }

    @Override
    public void noShowReservation(Long id, String waiterUsername) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reserva no encontrada con ID: " + id));
        verifyTenant(reservation);
        reservation.setStatus(ReservationStatus.NO_SHOW);
        reservationRepository.save(reservation);
        log.info("Reserva con ID: {} marcada como NO_SHOW por: {}.", id, waiterUsername);
        eventPublisher.publishEvent(new ReservationNoShowEvent(id, waiterUsername));
    }

    private void verifyTenant(Reservation reservation) {
        Long currentTenant = com.pezbackend.shared.infrastructure.TenantContext.getCurrentTenantId();
        if (currentTenant != null && !currentTenant.equals(reservation.getRestaurantId())) {
            throw new com.pezbackend.shared.domain.exceptions.TenantMismatchException("Reservation", reservation.getId());
        }
    }
}
