package com.pezbackend.orders.infrastructure.persistence.jpa.repositories;

import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.valueobjects.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de persistencia del agregado {@link Order}.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Busca la comanda activa asociada a una mesa física (que no esté en estado FREE ni PAID).
     *
     * @param tableId ID de la mesa
     * @param status  estado a omitir (usualmente OrderStatus.FREE o OrderStatus.PAID)
     * @return opcional con la comanda activa
     */
    Optional<Order> findByTableIdAndStatusNot(Long tableId, OrderStatus status);

    /**
     * Busca la comanda activa asociada a una mesa física (que esté en un estado en particular).
     *
     * @param tableId ID de la mesa
     * @param status  estado a buscar (ej. OrderStatus.TAKING_ORDER, OrderStatus.WAITING_DISHES)
     * @return opcional con la comanda
     */
    Optional<Order> findByTableIdAndStatus(Long tableId, OrderStatus status);

    /**
     * Recupera todas las comandas que se encuentren en un estado específico.
     *
     * @param status estado a filtrar
     * @return listado de comandas
     */
    List<Order> findAllByStatus(OrderStatus status);

    /**
     * Recupera todas las comandas que se encuentren en un estado específico ordenadas por su fecha de creación ascendente (FIFO).
     */
    List<Order> findAllByStatusOrderByCreatedAtAsc(OrderStatus status);

    /**
     * Recupera todas las comandas históricas o activas asociadas a una mesa.
     *
     * @param tableId ID de la mesa
     * @return listado de comandas
     */
    List<Order> findAllByTableId(Long tableId);
}
