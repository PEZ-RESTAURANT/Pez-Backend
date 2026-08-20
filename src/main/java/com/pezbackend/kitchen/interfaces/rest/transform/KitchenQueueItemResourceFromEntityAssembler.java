package com.pezbackend.kitchen.interfaces.rest.transform;

import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.kitchen.interfaces.rest.resources.KitchenQueueItemResource;

/**
 * Ensamblador para transformar entidades OrderItem en recursos KitchenQueueItemResource.
 */
public class KitchenQueueItemResourceFromEntityAssembler {

    /**
     * Convierte un OrderItem en su DTO de cocina.
     *
     * @param entity el OrderItem a convertir
     * @return el recurso DTO correspondiente
     */
    public static KitchenQueueItemResource toResourceFromEntity(OrderItem entity, Integer tableNumber) {
        return new KitchenQueueItemResource(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getNote(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                tableNumber,
                entity.getReadyAt(),
                entity.getOrder() != null && entity.getOrder().getType() != null ? entity.getOrder().getType().name() : "DINE_IN"
        );
    }
}
