package com.pezbackend.orders.domain.services;

import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio de comandos (Write) para el Bounded Context de Orders.
 */
public interface OrderCommandService {

    /**
     * Crea una nueva mesa en el salón.
     */
    RestaurantTable createTable(Integer number, Integer floor, String zoneTag, Integer positionX, Integer positionY);

    /**
     * Actualiza la posición física de una mesa en el mapa.
     */
    RestaurantTable updateTablePosition(Long tableId, Integer positionX, Integer positionY);

    /**
     * Actualiza los detalles administrativos de una mesa (número, piso y etiqueta de zona).
     */
    RestaurantTable updateTableDetails(Long tableId, Integer number, Integer floor, String zoneTag);

    /**
     * Elimina una mesa física si está libre y no tiene comandas activas.
     */
    void deleteTable(Long tableId);

    /**
     * Solicita atención de mozo para una mesa (transición FREE -> UNATTENDED).
     */
    void requestAttention(Long tableId);

    /**
     * Un mozo atiende la mesa y toma el control (transición UNATTENDED -> TAKING_ORDER).
     */
    void attendTable(Long tableId, String waiterUsername);

    /**
     * Crea un nuevo pedido (comanda) de mesa o para llevar/delivery.
     */
    Order createOrder(Long tableId, String typeStr, Long customerId);
    Order createOrder(Long tableId, String typeStr, Long customerId, String deliveryCustomerName, String deliveryCustomerPhone, String deliveryAddress, String deliveryMapsLink, String declaredPaymentMethod);

    /**
     * Comanda nuevos ítems (platos) a un pedido.
     */
    void addItemsToOrder(Long orderId, Long productId, Integer quantity, String note, Long waiterId, String waiterUsername);

    /**
     * Comanda un lote de platos a un pedido de forma atómica en una única transacción.
     */
    void addItemsToOrderBatch(Long orderId, java.util.List<com.pezbackend.orders.domain.model.valueobjects.AddOrderItemCommand> items, String waiterUsername);

    /**
     * Incrementa en 1 la cantidad de un ítem comandado.
     */
    void increaseItemQuantity(Long orderId, Long itemId, String executorUsername);

    /**
     * Decrementa en 1 la cantidad de un ítem comandado.
     */
    void decreaseItemQuantity(Long orderId, Long itemId, String executorUsername);

    /**
     * Cancela (anula) un ítem comandado requiriendo motivo (antes de preparación).
     */
    void cancelItem(Long orderId, Long itemId, String reasonCode, String detail, String executorUsername);

    /**
     * Elimina un ítem de la comanda requiriendo motivo (por error de digitación u otro).
     */
    void deleteItem(Long orderId, Long itemId, String reasonCode, String detail, String executorUsername);

    /**
     * Cambia el estado de preparación o entrega de un ítem (cocina o mozo).
     */
    void changeItemStatus(Long orderId, Long itemId, String statusStr, String executorUsername);

    /**
     * Aplica un ajuste manual de precio o descuento sobre el pedido.
     */
    void applyPriceAdjustment(Long orderId, String scopeStr, String validityStr,
                              LocalDateTime startAt, LocalDateTime endAt, BigDecimal newValue,
                              String reason, String executorUsername);

    /**
     * Emite la precuenta y cambia el estado a ISSUED_UNPAID (transición ALL_DELIVERED -> ISSUED_UNPAID).
     */
    void issueReceipt(Long orderId, String executorUsername);

    /**
     * Revierte la emisión de la precuenta regresando el estado del pedido y la mesa a ALL_DELIVERED.
     */
    void revertReceipt(Long orderId);

    /**
     * Registra el pago del pedido (transición ISSUED_UNPAID -> PAID -> FREE automático).
     */
    void markAsPaid(Long orderId);

    /**
     * Fusiona un grupo de mesas bajo una mesa ancla.
     */
    void mergeTables(Long anchorTableId, java.util.List<Long> tableIdsToMerge, String waiterUsername);

    /**
     * Deshace la fusión de mesas asociadas a una mesa ancla.
     */
    void unmergeTables(Long anchorTableId, String waiterUsername);

    /**
     * Traslada la comanda activa de una mesa a otra.
     */
    void transferOrder(Long fromTableId, Long toTableId, String waiterUsername);
}
