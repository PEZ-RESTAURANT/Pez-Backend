package com.pezbackend.orders.interfaces.rest.transform;

import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.interfaces.rest.resources.OrderResource;

/**
 * Mapeador de la entidad/agregado Order al recurso DTO.
 */
public class OrderResourceFromEntityAssembler {

    public static OrderResource toResourceFromEntity(Order entity) {
        if (entity == null) return null;
        return new OrderResource(
                entity.getId(),
                entity.getTableId(),
                entity.getType(),
                entity.getCustomerId(),
                entity.getStatus(),
                entity.getAttendedAt(),
                entity.getCreatedAt(),
                entity.getItems().stream()
                        .map(OrderItemResourceFromEntityAssembler::toResourceFromEntity)
                        .toList(),
                entity.getPriceAdjustments().stream()
                        .map(PriceAdjustmentResourceFromEntityAssembler::toResourceFromEntity)
                        .toList(),
                entity.getDeliveryCustomerName(),
                entity.getDeliveryCustomerPhone(),
                entity.getDeliveryAddress(),
                entity.getDeliveryMapsLink(),
                entity.getDeclaredPaymentMethod()
        );
    }
}
