package com.pezbackend.orders.application.internal;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.PriceAdjustment;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.model.events.*;
import com.pezbackend.orders.domain.model.valueobjects.*;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.InvalidStateTransitionException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de comandos para la gestión de mesas y comandas.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderCommandServiceImpl implements OrderCommandService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RestaurantTable createTable(Integer number, Integer floor, String zoneTag, Integer positionX, Integer positionY) {
        if (restaurantTableRepository.existsByNumber(number)) {
            throw new BusinessRuleViolationException(
                    "TABLE_EXISTS",
                    "Ya existe una mesa con el número: " + number
            );
        }
        RestaurantTable table = new RestaurantTable(number, floor, zoneTag, positionX, positionY);
        return restaurantTableRepository.save(table);
    }

    @Override
    public RestaurantTable updateTablePosition(Long tableId, Integer positionX, Integer positionY) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));
        table.setPositionX(positionX);
        table.setPositionY(positionY);
        return restaurantTableRepository.save(table);
    }

    @Override
    public void requestAttention(Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));

        if (table.getStatus() != TableStatus.FREE) {
            throw new InvalidStateTransitionException(
                    "INVALID_TABLE_STATE",
                    "Solo se puede solicitar atención en una mesa que esté libre."
            );
        }

        // Transición de mesa a UNATTENDED
        table.setStatus(TableStatus.UNATTENDED);
        restaurantTableRepository.save(table);

        // Crear comanda asociada
        Order order = new Order(table.getId(), OrderType.DINE_IN, null);
        order.setStatus(OrderStatus.UNATTENDED);
        orderRepository.save(order);

        log.info("Mesa {} solicitó atención. Comanda activa creada.", table.getNumber());
        eventPublisher.publishEvent(new TableAttentionRequestedEvent(table.getId(), table.getNumber()));
    }

    @Override
    public void attendTable(Long tableId, String waiterUsername) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));

        if (table.getStatus() != TableStatus.UNATTENDED) {
            throw new InvalidStateTransitionException(
                    "INVALID_TABLE_STATE",
                    "Solo se puede atender una mesa que esté en espera de atención (UNATTENDED)."
            );
        }

        // Buscar comanda activa en estado UNATTENDED
        Order order = orderRepository.findByTableIdAndStatus(table.getId(), OrderStatus.UNATTENDED)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "No se encontró comanda en espera para la mesa: " + table.getNumber()));

        // Transicionar estados
        order.transitionTo(OrderStatus.TAKING_ORDER);
        table.setStatus(TableStatus.TAKING_ORDER);

        orderRepository.save(order);
        restaurantTableRepository.save(table);

        log.info("Mesa {} atendida por el mozo {}.", table.getNumber(), waiterUsername);
        eventPublisher.publishEvent(new TableAttendedEvent(table.getId(), table.getNumber(), waiterUsername));
    }

    @Override
    public Order createOrder(Long tableId, String typeStr, Long customerId) {
        OrderType type = OrderType.valueOf(typeStr.toUpperCase());
        
        if (type == OrderType.DINE_IN) {
            if (tableId == null) {
                throw new BusinessRuleViolationException("TABLE_REQUIRED", "El ID de la mesa es obligatorio para consumo en salón (DINE_IN).");
            }
            RestaurantTable table = restaurantTableRepository.findById(tableId)
                    .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));

            if (table.getStatus() == TableStatus.FREE) {
                // Mozo inicia directamente la comanda en la mesa
                table.setStatus(TableStatus.TAKING_ORDER);
                restaurantTableRepository.save(table);

                Order order = new Order(table.getId(), OrderType.DINE_IN, customerId);
                order.setStatus(OrderStatus.TAKING_ORDER);
                order.setAttendedAt(LocalDateTime.now());
                return orderRepository.save(order);

            } else if (table.getStatus() == TableStatus.UNATTENDED) {
                // Se atiende la solicitud de atención existente
                Order order = orderRepository.findByTableIdAndStatus(table.getId(), OrderStatus.UNATTENDED)
                        .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "No se encontró comanda en espera para la mesa: " + table.getNumber()));

                order.transitionTo(OrderStatus.TAKING_ORDER);
                table.setStatus(TableStatus.TAKING_ORDER);

                restaurantTableRepository.save(table);
                return orderRepository.save(order);

            } else {
                throw new InvalidStateTransitionException(
                        "TABLE_OCCUPIED",
                        "La mesa " + table.getNumber() + " ya está ocupada o en un estado no editable."
                );
            }
        } else {
            // TAKEAWAY o DELIVERY (no requieren mesa física)
            if (tableId != null) {
                throw new BusinessRuleViolationException("TABLE_NOT_ALLOWED", "No se permite asociar mesa para pedidos TAKEAWAY o DELIVERY.");
            }
            Order order = new Order(null, type, customerId);
            order.setStatus(OrderStatus.TAKING_ORDER);
            order.setAttendedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
    }

    @Override
    public void addItemsToOrder(Long orderId, Long productId, Integer quantity, String note, Long waiterId, String waiterUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FREE) {
            throw new BusinessRuleViolationException("ORDER_NOT_EDITABLE", "No se pueden agregar platos a una comanda cerrada o pagada.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Producto no encontrado con ID: " + productId));

        if (quantity == null || quantity <= 0) {
            throw new BusinessRuleViolationException("INVALID_QUANTITY", "La cantidad debe ser mayor que cero.");
        }

        OrderItem item = new OrderItem(product.getId(), quantity, note, waiterId, product.getPrice());
        order.addItem(item);

        // Si estaba en TAKING_ORDER o ALL_DELIVERED, pasa automáticamente a WAITING_DISHES
        if (order.getStatus() == OrderStatus.TAKING_ORDER || order.getStatus() == OrderStatus.ALL_DELIVERED) {
            order.transitionTo(OrderStatus.WAITING_DISHES);
            if (order.getTableId() != null) {
                RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElseThrow();
                table.setStatus(TableStatus.WAITING_DISHES);
                restaurantTableRepository.save(table);
            }
        }

        orderRepository.save(order);
        // Recuperar el ID asignado por JPA tras guardar
        Long assignedItemId = order.getItems().stream()
                .filter(i -> i.getProductId().equals(productId) && i.getQuantity().equals(quantity) && i.getWaiterId().equals(waiterId))
                .map(OrderItem::getId)
                .findFirst()
                .orElse(null);

        log.info("Ítem agregado a la comanda: Producto ID {}, Cantidad {}.", productId, quantity);
        eventPublisher.publishEvent(new ItemOrderedEvent(order.getId(), assignedItemId, productId, quantity, waiterId, waiterUsername));
    }

    @Override
    public void increaseItemQuantity(Long orderId, Long itemId, String executorUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FREE) {
            throw new BusinessRuleViolationException("ORDER_NOT_EDITABLE", "No se puede modificar una comanda cerrada.");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ITEM_NOT_FOUND", "Ítem no encontrado en la comanda con ID: " + itemId));

        int oldQty = item.getQuantity();
        int newQty = oldQty + 1;
        item.setQuantity(newQty);

        // Si estaba en ALL_DELIVERED, vuelve a WAITING_DISHES
        if (order.getStatus() == OrderStatus.ALL_DELIVERED) {
            order.transitionTo(OrderStatus.WAITING_DISHES);
            if (order.getTableId() != null) {
                RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElseThrow();
                table.setStatus(TableStatus.WAITING_DISHES);
                restaurantTableRepository.save(table);
            }
        }

        orderRepository.save(order);
        eventPublisher.publishEvent(new ItemModifiedEvent(order.getId(), item.getId(), oldQty, newQty, executorUsername));
    }

    @Override
    public void decreaseItemQuantity(Long orderId, Long itemId, String executorUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FREE) {
            throw new BusinessRuleViolationException("ORDER_NOT_EDITABLE", "No se puede modificar una comanda cerrada.");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ITEM_NOT_FOUND", "Ítem no encontrado en la comanda con ID: " + itemId));

        int oldQty = item.getQuantity();
        if (oldQty <= 1) {
            throw new BusinessRuleViolationException(
                    "INVALID_QTY_DECREASE",
                    "La cantidad no puede ser menor a 1. Si desea anular el plato, use los endpoints de cancelación."
            );
        }

        int newQty = oldQty - 1;
        item.setQuantity(newQty);

        orderRepository.save(order);
        eventPublisher.publishEvent(new ItemModifiedEvent(order.getId(), item.getId(), oldQty, newQty, executorUsername));
    }

    @Override
    public void cancelItem(Long orderId, Long itemId, String reasonCode, String detail, String executorUsername) {
        performItemRemoval(orderId, itemId, reasonCode, detail, executorUsername);
    }

    @Override
    public void deleteItem(Long orderId, Long itemId, String reasonCode, String detail, String executorUsername) {
        performItemRemoval(orderId, itemId, reasonCode, detail, executorUsername);
    }

    private void performItemRemoval(Long orderId, Long itemId, String reasonCode, String detail, String executorUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FREE) {
            throw new BusinessRuleViolationException("ORDER_NOT_EDITABLE", "No se puede modificar una comanda cerrada.");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ITEM_NOT_FOUND", "Ítem no encontrado en la comanda con ID: " + itemId));

        // Validaciones de motivo obligatorio si es OTHER
        CancellationReason reason = CancellationReason.valueOf(reasonCode.toUpperCase());
        if (reason == CancellationReason.OTHER && (detail == null || detail.strip().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "DETAIL_REQUIRED",
                    "Debe proporcionar una justificación detallada cuando el motivo es 'OTHER'."
            );
        }

        Long originalWaiterId = item.getWaiterId();

        // Remover de la colección (gatilla orphanRemoval)
        order.getItems().remove(item);

        // Verificar si la comanda queda vacía, o si todos los platos restantes ya fueron entregados
        checkAndTriggerAllDelivered(order);

        orderRepository.save(order);
        log.info("Ítem ID {} removido/cancelado de la comanda {} por {}.", itemId, orderId, executorUsername);
        eventPublisher.publishEvent(new ItemCancelledEvent(order.getId(), itemId, originalWaiterId, reason.name(), detail, executorUsername));
    }

    @Override
    public void changeItemStatus(Long orderId, Long itemId, String statusStr, String executorUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ITEM_NOT_FOUND", "Ítem no encontrado en la comanda con ID: " + itemId));

        OrderItemStatus oldStatus = item.getStatus();
        OrderItemStatus newStatus = OrderItemStatus.valueOf(statusStr.toUpperCase());

        if (oldStatus == newStatus) {
            return;
        }

        item.setStatus(newStatus);

        if (newStatus == OrderItemStatus.READY) {
            item.setReadyAt(LocalDateTime.now());
        } else if (newStatus == OrderItemStatus.DELIVERED) {
            item.setDeliveredAt(LocalDateTime.now());
            // Verificar si todos los ítems están en DELIVERED
            checkAndTriggerAllDelivered(order);
        }

        orderRepository.save(order);
        eventPublisher.publishEvent(new ItemStatusChangedEvent(order.getId(), item.getId(), oldStatus.name(), newStatus.name(), executorUsername));
    }

    private void checkAndTriggerAllDelivered(Order order) {
        if (order.getItems().isEmpty()) {
            return;
        }

        boolean allDelivered = order.getItems().stream()
                .allMatch(i -> i.getStatus() == OrderItemStatus.DELIVERED);

        if (allDelivered && order.getStatus() == OrderStatus.WAITING_DISHES) {
            order.transitionTo(OrderStatus.ALL_DELIVERED);
            if (order.getTableId() != null) {
                RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElseThrow();
                table.setStatus(TableStatus.ALL_DELIVERED);
                restaurantTableRepository.save(table);
            }
            log.info("Todos los platos de la comanda {} han sido entregados. Estado cambiado a ALL_DELIVERED.", order.getId());
            eventPublisher.publishEvent(new AllItemsDeliveredEvent(order.getId()));
        }
    }

    @Override
    public void applyPriceAdjustment(Long orderId, String scopeStr, String validityStr,
                                     LocalDateTime startAt, LocalDateTime endAt, BigDecimal newValue,
                                     String reason, String executorUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FREE) {
            throw new BusinessRuleViolationException("ORDER_NOT_EDITABLE", "No se puede modificar una comanda cerrada.");
        }

        PriceAdjustmentScope scope = PriceAdjustmentScope.valueOf(scopeStr.toUpperCase());
        PriceAdjustmentValidity validity = PriceAdjustmentValidity.valueOf(validityStr.toUpperCase());

        if (newValue == null || newValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_VALUE", "El nuevo valor ajustado no puede ser negativo.");
        }

        if (reason == null || reason.strip().isEmpty()) {
            throw new BusinessRuleViolationException("REASON_REQUIRED", "El motivo del ajuste de precio es obligatorio.");
        }

        PriceAdjustment adjustment = new PriceAdjustment(scope, validity, startAt, endAt, newValue, executorUsername, reason);
        order.addPriceAdjustment(adjustment);

        orderRepository.save(order);
        log.info("Ajuste de precio aplicado en comanda {} por {}.", orderId, executorUsername);
        eventPublisher.publishEvent(new PriceAdjustmentAppliedEvent(order.getId(), scope.name(), validity.name(), newValue, reason, executorUsername));
    }

    @Override
    public void issueReceipt(Long orderId, String executorUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        // En esta fase, solo disparamos la transición de ALL_DELIVERED -> ISSUED_UNPAID
        order.transitionTo(OrderStatus.ISSUED_UNPAID);

        if (order.getTableId() != null) {
            RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElseThrow();
            table.setStatus(TableStatus.ISSUED_UNPAID);
            restaurantTableRepository.save(table);
        }

        orderRepository.save(order);
        log.info("Precuenta emitida para comanda {}. Cambiado a ISSUED_UNPAID.", orderId);
        eventPublisher.publishEvent(new ReceiptIssuedEvent(order.getId(), executorUsername));
    }

    @Override
    public void markAsPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        // Transiciona a PAID
        order.transitionTo(OrderStatus.PAID);
        Long tableId = order.getTableId();
        
        RestaurantTable table = null;
        if (tableId != null) {
            table = restaurantTableRepository.findById(tableId).orElseThrow();
            table.setStatus(TableStatus.PAID);
            restaurantTableRepository.save(table);
        }
        orderRepository.save(order);
        log.info("Comanda {} marcada como pagada (PAID).", orderId);

        // PAID -> FREE (automático: libera la mesa)
        order.transitionTo(OrderStatus.FREE);
        if (table != null) {
            table.setStatus(TableStatus.FREE);
            restaurantTableRepository.save(table);
            log.info("Mesa {} liberada (FREE).", table.getNumber());
            eventPublisher.publishEvent(new TableReleasedEvent(table.getId(), table.getNumber()));
        }
        orderRepository.save(order);
    }
}
