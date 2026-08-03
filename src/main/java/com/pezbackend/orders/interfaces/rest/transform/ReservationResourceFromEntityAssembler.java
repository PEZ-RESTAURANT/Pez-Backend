package com.pezbackend.orders.interfaces.rest.transform;

import com.pezbackend.orders.domain.model.entities.Reservation;
import com.pezbackend.orders.interfaces.rest.resources.ReservationResource;

/**
 * Mapeador de la entidad Reservation al recurso DTO.
 */
public class ReservationResourceFromEntityAssembler {

    public static ReservationResource toResourceFromEntity(Reservation entity) {
        if (entity == null) return null;
        return new ReservationResource(
                entity.getId(),
                entity.getCustomerName(),
                entity.getCustomerPhone(),
                entity.getCustomerId(),
                entity.getReservationDateTime(),
                entity.getPartySize(),
                entity.getNotes(),
                entity.getTableId(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
