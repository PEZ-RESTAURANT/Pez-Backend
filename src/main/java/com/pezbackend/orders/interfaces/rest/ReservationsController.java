package com.pezbackend.orders.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.orders.domain.model.entities.Reservation;
import com.pezbackend.orders.domain.services.ReservationCommandService;
import com.pezbackend.orders.domain.services.ReservationQueryService;
import com.pezbackend.orders.interfaces.rest.resources.CreateReservationResource;
import com.pezbackend.orders.interfaces.rest.resources.ReservationResource;
import com.pezbackend.orders.interfaces.rest.resources.UpdateReservationResource;
import com.pezbackend.orders.interfaces.rest.transform.ReservationResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la gestión de reservas de mesas.
 */
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Endpoints para la reserva e histórico de atención de mesas")
public class ReservationsController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    @PostMapping
    @RequiresPermission("reservations.manage")
    public ResponseEntity<ReservationResource> createReservation(@Valid @RequestBody CreateReservationResource resource) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Reservation reservation = reservationCommandService.createReservation(
                resource.customerName(),
                resource.customerPhone(),
                resource.customerId(),
                resource.reservationDateTime(),
                resource.partySize(),
                resource.notes(),
                resource.tableId(),
                username
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation));
    }

    @GetMapping
    @RequiresPermission("reservations.view")
    public ResponseEntity<List<ReservationResource>> getReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status
    ) {
        List<Reservation> reservations = reservationQueryService.getReservations(date, status);
        List<ReservationResource> resources = reservations.stream()
                .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @RequiresPermission("reservations.manage")
    public ResponseEntity<ReservationResource> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationResource resource
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Reservation reservation = reservationCommandService.updateReservation(
                id,
                resource.customerName(),
                resource.customerPhone(),
                resource.customerId(),
                resource.reservationDateTime(),
                resource.partySize(),
                resource.notes(),
                resource.tableId(),
                resource.status(),
                username
        );
        return ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation));
    }

    @PostMapping("/{id}/cancel")
    @RequiresPermission("reservations.manage")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        reservationCommandService.cancelReservation(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    @RequiresPermission("reservations.manage")
    public ResponseEntity<Void> completeReservation(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        reservationCommandService.completeReservation(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/no-show")
    @RequiresPermission("reservations.manage")
    public ResponseEntity<Void> noShowReservation(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        reservationCommandService.noShowReservation(id, username);
        return ResponseEntity.noContent().build();
    }
}
