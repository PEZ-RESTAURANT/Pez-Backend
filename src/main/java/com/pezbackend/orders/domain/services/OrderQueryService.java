package com.pezbackend.orders.domain.services;

import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;

import java.util.List;

/**
 * Servicio de consultas (Read) para el Bounded Context de Orders.
 */
public interface OrderQueryService {

    /**
     * Obtiene todas las mesas registradas en el local.
     *
     * @return lista de todas las mesas
     */
    List<RestaurantTable> getAllTables();

    /**
     * Obtiene una mesa por su ID único.
     *
     * @param id ID de la mesa
     * @return la mesa encontrada
     */
    RestaurantTable getTableById(Long id);

    /**
     * Obtiene todas las comandas en el sistema.
     *
     * @return lista de todas las comandas
     */
    List<Order> getAllOrders();

    /**
     * Obtiene una comanda por su ID único.
     *
     * @param id ID de la comanda
     * @return la comanda encontrada
     */
    Order getOrderById(Long id);

    /**
     * Obtiene la cola de atención actual (mesas en UNATTENDED, ordenadas FIFO).
     *
     * @return lista de mesas en la cola de atención
     */
    List<RestaurantTable> getAttentionQueue();

    /**
     * Obtiene la cola de cocina actual (platos en PENDING o IN_PREPARATION, ordenados FIFO).
     * El zoneId es opcional en esta fase.
     *
     * @param zoneId ID de la zona de preparación (opcional)
     * @return lista de ítems de comanda en cola de preparación
     */
    List<OrderItem> getKitchenQueue(Long zoneId);

    /**
     * Obtiene el grupo de mesas fusionadas asociadas (incluye el ancla y las fusionadas).
     */
    List<RestaurantTable> getMergeGroup(Long tableId);
}
