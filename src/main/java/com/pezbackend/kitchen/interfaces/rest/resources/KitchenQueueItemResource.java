package com.pezbackend.kitchen.interfaces.rest.resources;

import java.time.LocalDateTime;

/**
 * Recurso DTO que representa un plato/item en la cola de cocina para el cocinero.
 *
 * @param id          ID del item de comanda (OrderItem)
 * @param orderId     ID de la comanda asociada (Order)
 * @param productId   ID del producto
 * @param quantity    cantidad solicitada
 * @param note        nota/comentario de preparación del mozo
 * @param status      estado actual del plato (PENDING, IN_PREPARATION)
 * @param createdAt   fecha y hora de creación de la comanda
 */
public record KitchenQueueItemResource(
        Long id,
        Long orderId,
        Long productId,
        Integer quantity,
        String note,
        String status,
        LocalDateTime createdAt,
        Integer tableNumber,
        LocalDateTime readyAt,
        String orderType
) {}
