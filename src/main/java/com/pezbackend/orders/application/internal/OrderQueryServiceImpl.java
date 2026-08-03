package com.pezbackend.orders.application.internal;

import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.model.valueobjects.OrderItemStatus;
import com.pezbackend.orders.domain.model.valueobjects.OrderStatus;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Implementación del servicio de consultas para el Bounded Context de Orders.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<RestaurantTable> getAllTables() {
        return restaurantTableRepository.findAll();
    }

    @Override
    public RestaurantTable getTableById(Long id) {
        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + id));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + id));
    }

    @Override
    public List<RestaurantTable> getAttentionQueue() {
        List<Order> unattendedOrders = orderRepository.findAllByStatusOrderByCreatedAtAsc(OrderStatus.UNATTENDED);
        return unattendedOrders.stream()
                .map(order -> restaurantTableRepository.findById(order.getTableId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<OrderItem> getKitchenQueue(Long zoneId) {
        // En esta fase ignoramos el filtrado por zoneId ya que no existe catálogo de zonas integrado en el ítem.
        // Recuperamos todos los pedidos activos.
        List<Order> activeOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.FREE && o.getStatus() != OrderStatus.PAID)
                .toList();

        return activeOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .filter(item -> item.getStatus() == OrderItemStatus.PENDING || item.getStatus() == OrderItemStatus.IN_PREPARATION)
                .sorted(Comparator.comparing(OrderItem::getCreatedAt))
                .toList();
    }

    @Override
    public List<RestaurantTable> getMergeGroup(Long tableId) {
        RestaurantTable table = getTableById(tableId);
        Long anchorId = table.getAnchorTableId() != null ? table.getAnchorTableId() : table.getId();

        RestaurantTable anchorTable = getTableById(anchorId);
        List<RestaurantTable> merged = restaurantTableRepository.findAllByAnchorTableId(anchorId);

        java.util.List<RestaurantTable> group = new java.util.ArrayList<>();
        group.add(anchorTable);
        group.addAll(merged);
        return group;
    }
}
