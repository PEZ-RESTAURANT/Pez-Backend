package com.pezbackend.orders.interfaces.rest.transform;

import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.interfaces.rest.resources.OrderItemResource;

/**
 * Mapeador de la entidad OrderItem al recurso DTO.
 */
public class OrderItemResourceFromEntityAssembler {

    public static OrderItemResource toResourceFromEntity(OrderItem entity) {
        if (entity == null) return null;
        return new OrderItemResource(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getNote(),
                entity.getWaiterId(),
                entity.getUnitPriceSnapshot(),
                entity.getStatus(),
                entity.getReadyAt(),
                entity.getDeliveredAt(),
                entity.getCreatedAt()
        );
    }
}
