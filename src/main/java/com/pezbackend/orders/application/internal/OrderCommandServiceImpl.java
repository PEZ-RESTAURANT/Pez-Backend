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
import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditEventRepository auditEventRepository;

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
    public RestaurantTable updateTableDetails(Long tableId, Integer number, Integer floor, String zoneTag) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));

        if (!table.getNumber().equals(number) && restaurantTableRepository.existsByNumber(number)) {
            throw new BusinessRuleViolationException(
                    "TABLE_EXISTS",
                    "Ya existe otra mesa con el número: " + number
            );
        }

        table.setNumber(number);
        table.setFloor(floor);
        table.setZoneTag(zoneTag);
        return restaurantTableRepository.save(table);
    }

    @Override
    public void deleteTable(Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));

        if (table.getStatus() != TableStatus.FREE) {
            throw new BusinessRuleViolationException(
                    "TABLE_ACTIVE",
                    "No se puede eliminar una mesa que no esté libre (estado actual: " + table.getStatus() + ")"
            );
        }

        // Validación de fusión de mesas
        if (table.getAnchorTableId() != null) {
            throw new BusinessRuleViolationException(
                    "TABLE_MERGED",
                    "No se puede eliminar una mesa fusionada. Deshaga la fusión primero."
            );
        }

        boolean isAnchor = !restaurantTableRepository.findAllByAnchorTableId(table.getId()).isEmpty();
        if (isAnchor) {
            throw new BusinessRuleViolationException(
                    "TABLE_IS_ANCHOR",
                    "No se puede eliminar una mesa que actúa como ancla de un grupo fusionado. Deshaga la fusión primero."
            );
        }

        // Además verificamos comanda activa en base de datos para redundancia de seguridad
        java.util.Optional<Order> activeOrder = orderRepository.findByTableIdAndStatusNot(tableId, OrderStatus.PAID);
        if (activeOrder.isPresent() && activeOrder.get().getStatus() != OrderStatus.FREE) {
            throw new BusinessRuleViolationException(
                    "TABLE_HAS_ACTIVE_ORDER",
                    "No se puede eliminar una mesa con un pedido activo."
            );
        }

        restaurantTableRepository.delete(table);
    }

    @Override
    public void requestAttention(Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));

        if (table.getAnchorTableId() != null) {
            throw new BusinessRuleViolationException(
                    "TABLE_MERGED",
                    "No se puede solicitar atención en una mesa fusionada. Debe hacerlo contra la mesa ancla."
            );
        }

        if (table.getStatus() != TableStatus.FREE) {
            throw new InvalidStateTransitionException(
                    "INVALID_TABLE_STATE",
                    "Solo se puede solicitar atención en una mesa que esté libre."
            );
        }

        // Transición de mesa a UNATTENDED
        table.setStatus(TableStatus.UNATTENDED);
        syncMergedTablesStatus(table.getId(), TableStatus.UNATTENDED);
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

        // Sincronizar mesas fusionadas
        syncMergedTablesStatus(table.getId(), TableStatus.TAKING_ORDER);

        orderRepository.save(order);
        restaurantTableRepository.save(table);

        log.info("Mesa {} atendida por el mozo {}.", table.getNumber(), waiterUsername);
        eventPublisher.publishEvent(new TableAttendedEvent(table.getId(), table.getNumber(), waiterUsername));
    }

    @Override
    public Order createOrder(Long tableId, String typeStr, Long customerId) {
        return createOrder(tableId, typeStr, customerId, null, null, null, null, null, false);
    }

    @Override
    public Order createOrder(Long tableId, String typeStr, Long customerId,
                             String deliveryCustomerName, String deliveryCustomerPhone,
                             String deliveryAddress, String deliveryMapsLink,
                             String declaredPaymentMethod, Boolean ignoreDuplicatePhone) {
        OrderType type = OrderType.valueOf(typeStr.toUpperCase());
        
        if (type == OrderType.DINE_IN) {
            if (tableId == null) {
                throw new BusinessRuleViolationException("TABLE_REQUIRED", "El ID de la mesa es obligatorio para consumo en salón (DINE_IN).");
            }
            RestaurantTable table = restaurantTableRepository.findById(tableId)
                    .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa no encontrada con ID: " + tableId));

            if (table.getAnchorTableId() != null) {
                throw new BusinessRuleViolationException(
                        "TABLE_MERGED",
                        "No se puede iniciar un pedido en una mesa fusionada. Debe hacerlo contra la mesa ancla."
                );
            }

            if (table.getStatus() == TableStatus.FREE) {
                // Mozo inicia directamente la comanda en la mesa
                table.setStatus(TableStatus.TAKING_ORDER);
                syncMergedTablesStatus(table.getId(), TableStatus.TAKING_ORDER);
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
                syncMergedTablesStatus(table.getId(), TableStatus.TAKING_ORDER);

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
            
            // Si es DELIVERY, validar campos obligatorios y guardar/actualizar cliente
            if (type == OrderType.DELIVERY) {
                if (deliveryCustomerName == null || deliveryCustomerName.isBlank()) {
                    throw new BusinessRuleViolationException("DELIVERY_CUSTOMER_NAME_REQUIRED", "El nombre del cliente es obligatorio para pedidos DELIVERY.");
                }
                if (deliveryCustomerPhone == null || deliveryCustomerPhone.isBlank()) {
                    throw new BusinessRuleViolationException("DELIVERY_CUSTOMER_PHONE_REQUIRED", "El teléfono del cliente es obligatorio para pedidos DELIVERY.");
                }
                if (deliveryAddress == null || deliveryAddress.isBlank()) {
                    throw new BusinessRuleViolationException("DELIVERY_ADDRESS_REQUIRED", "La dirección de entrega es obligatoria para pedidos DELIVERY.");
                }
                if (declaredPaymentMethod == null || declaredPaymentMethod.isBlank()) {
                    throw new BusinessRuleViolationException("DELIVERY_PAYMENT_METHOD_REQUIRED", "El método de pago declarado es obligatorio para pedidos DELIVERY.");
                }

                // Validar número duplicado en delivery activo
                List<Order> activeDeliveries = orderRepository.findAllByTypeAndDeliveryCustomerPhoneAndStatusNot(
                        OrderType.DELIVERY, deliveryCustomerPhone, OrderStatus.PAID
                );
                if (!activeDeliveries.isEmpty()) {
                    if (ignoreDuplicatePhone == null || !ignoreDuplicatePhone) {
                        throw new BusinessRuleViolationException("DUPLICATE_DELIVERY_PHONE", "Ya hay un delivery en curso con este número.");
                    } else {
                        // Trazar el bypass en eventos de auditoría
                        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                        String username = "system";
                        if (auth != null && auth.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
                            username = userDetails.getUsername();
                        }
                        com.pezbackend.shared.domain.model.AuditEvent auditEvent = new com.pezbackend.shared.domain.model.AuditEvent(
                                "DuplicateDeliveryPhoneBypassed",
                                "orders",
                                username,
                                null,
                                java.util.Map.of(
                                    "deliveryCustomerPhone", deliveryCustomerPhone,
                                    "deliveryCustomerName", deliveryCustomerName,
                                    "action", "Bypassed duplicate delivery phone warning"
                                ),
                                "Continuar con número duplicado",
                                LocalDateTime.now()
                        );
                        auditEventRepository.save(auditEvent);
                    }
                }

                // Guardar/Actualizar cliente de forma automática para autocompletado y obligaciones de registro
                java.util.Optional<Customer> customerOpt = customerRepository.findByPhone(deliveryCustomerPhone);
                Customer customer;
                if (customerOpt.isPresent()) {
                    customer = customerOpt.get();
                    customer.setFullName(deliveryCustomerName);
                    customer.setAddress(deliveryAddress);
                    customer.setLastPaymentMethod(declaredPaymentMethod);
                    customer = customerRepository.save(customer);
                } else {
                    customer = new Customer(deliveryCustomerPhone, deliveryCustomerName, null, deliveryAddress, false);
                    customer.setLastPaymentMethod(declaredPaymentMethod);
                    customer = customerRepository.save(customer);
                }
                
                if (customerId == null) {
                    customerId = customer.getId();
                }
            }

            Order order = new Order(null, type, customerId);
            order.setStatus(OrderStatus.TAKING_ORDER);
            order.setAttendedAt(LocalDateTime.now());
            
            order.setDeliveryCustomerName(deliveryCustomerName);
            order.setDeliveryCustomerPhone(deliveryCustomerPhone);
            order.setDeliveryAddress(deliveryAddress);
            order.setDeliveryMapsLink(deliveryMapsLink);
            order.setDeclaredPaymentMethod(declaredPaymentMethod);

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
                syncMergedTablesStatus(table.getId(), TableStatus.WAITING_DISHES);
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
    public void addItemsToOrderBatch(Long orderId, java.util.List<com.pezbackend.orders.domain.model.valueobjects.AddOrderItemCommand> items, String waiterUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FREE) {
            throw new BusinessRuleViolationException("ORDER_NOT_EDITABLE", "No se pueden agregar platos a una comanda cerrada o pagada.");
        }

        boolean transitionNeeded = false;

        for (com.pezbackend.orders.domain.model.valueobjects.AddOrderItemCommand cmd : items) {
            Product product = productRepository.findById(cmd.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Producto no encontrado con ID: " + cmd.productId()));

            if (cmd.quantity() == null || cmd.quantity() <= 0) {
                throw new BusinessRuleViolationException("INVALID_QUANTITY", "La cantidad debe ser mayor que cero.");
            }

            OrderItem item = new OrderItem(product.getId(), cmd.quantity(), cmd.note(), cmd.waiterId(), product.getPrice());
            order.addItem(item);

            if (order.getStatus() == OrderStatus.TAKING_ORDER || order.getStatus() == OrderStatus.ALL_DELIVERED) {
                transitionNeeded = true;
            }
        }

        if (transitionNeeded) {
            order.transitionTo(OrderStatus.WAITING_DISHES);
            if (order.getTableId() != null) {
                RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElseThrow();
                table.setStatus(TableStatus.WAITING_DISHES);
                syncMergedTablesStatus(table.getId(), TableStatus.WAITING_DISHES);
                restaurantTableRepository.save(table);
            }
        }

        orderRepository.save(order);

        // Publicar eventos de dominio para cada item después de guardar
        for (com.pezbackend.orders.domain.model.valueobjects.AddOrderItemCommand cmd : items) {
            Long assignedItemId = order.getItems().stream()
                    .filter(i -> i.getProductId().equals(cmd.productId()) 
                              && i.getQuantity().equals(cmd.quantity()) 
                              && i.getWaiterId().equals(cmd.waiterId())
                              && java.util.Objects.equals(i.getNote(), cmd.note()))
                    .map(OrderItem::getId)
                    .findFirst()
                    .orElse(null);

            log.info("Ítem agregado a la comanda en lote: Producto ID {}, Cantidad {}.", cmd.productId(), cmd.quantity());
            eventPublisher.publishEvent(new ItemOrderedEvent(order.getId(), assignedItemId, cmd.productId(), cmd.quantity(), cmd.waiterId(), waiterUsername));
        }
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

        if (order.getItems().isEmpty()) {
            order.transitionTo(OrderStatus.FREE);
            if (order.getTableId() != null) {
                RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
                if (table != null) {
                    table.setStatus(TableStatus.FREE);
                    restaurantTableRepository.save(table);
                    log.info("Mesa {} liberada por comanda vacía.", table.getNumber());
                    releaseMergedTables(table.getId());
                    eventPublisher.publishEvent(new TableReleasedEvent(table.getId(), table.getNumber()));
                }
            }
        } else {
            // Verificar si todos los platos restantes ya fueron entregados
            checkAndTriggerAllDelivered(order);
        }

        orderRepository.save(order);
        log.info("Ítem ID {} removido/cancelado de la comanda {} por {}.", itemId, orderId, executorUsername);
        eventPublisher.publishEvent(new ItemCancelledEvent(
                order.getId(),
                itemId,
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPriceSnapshot(),
                originalWaiterId,
                reason.name(),
                detail,
                executorUsername
        ));
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
        eventPublisher.publishEvent(new ItemStatusChangedEvent(
                order.getId(),
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                oldStatus.name(),
                newStatus.name(),
                executorUsername
        ));
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
                syncMergedTablesStatus(table.getId(), TableStatus.ALL_DELIVERED);
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
            syncMergedTablesStatus(table.getId(), TableStatus.ISSUED_UNPAID);
            restaurantTableRepository.save(table);
        }

        orderRepository.save(order);
        log.info("Precuenta emitida para comanda {}. Cambiado a ISSUED_UNPAID.", orderId);
        eventPublisher.publishEvent(new ReceiptIssuedEvent(order.getId(), executorUsername));
    }

    @Override
    @Transactional
    public void revertReceipt(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Pedido no encontrado con ID: " + orderId));

        order.transitionTo(OrderStatus.ALL_DELIVERED);

        if (order.getTableId() != null) {
            RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElseThrow();
            table.setStatus(TableStatus.ALL_DELIVERED);
            syncMergedTablesStatus(table.getId(), TableStatus.ALL_DELIVERED);
            restaurantTableRepository.save(table);
        }

        orderRepository.save(order);
        log.info("Precuenta revertida para comanda {}. Cambiado a ALL_DELIVERED.", orderId);
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
            syncMergedTablesStatus(table.getId(), TableStatus.PAID);
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
            
            // Liberar automáticamente todas las mesas fusionadas y limpiar su anchorTableId
            releaseMergedTables(table.getId());
            
            eventPublisher.publishEvent(new TableReleasedEvent(table.getId(), table.getNumber()));
        }
        orderRepository.save(order);
    }

    @Override
    public void mergeTables(Long anchorTableId, List<Long> tableIdsToMerge, String waiterUsername) {
        RestaurantTable anchorTable = restaurantTableRepository.findById(anchorTableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa ancla no encontrada con ID: " + anchorTableId));

        if (anchorTable.getAnchorTableId() != null) {
            throw new com.pezbackend.orders.domain.exceptions.TableAlreadyMergedException(
                    "La mesa ancla seleccionada ya está fusionada a otra mesa."
            );
        }

        List<Long> actualMergedIds = new ArrayList<>();
        for (Long tableId : tableIdsToMerge) {
            if (tableId.equals(anchorTableId)) {
                continue;
            }

            RestaurantTable table = restaurantTableRepository.findById(tableId)
                    .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa a fusionar no encontrada con ID: " + tableId));

            if (table.getStatus() != TableStatus.FREE) {
                throw new com.pezbackend.orders.domain.exceptions.TableNotAvailableException(
                        "La mesa a fusionar con número " + table.getNumber() + " no está disponible (estado actual: " + table.getStatus() + ")."
                );
            }

            if (table.getAnchorTableId() != null) {
                throw new com.pezbackend.orders.domain.exceptions.TableAlreadyMergedException(
                        "La mesa con número " + table.getNumber() + " ya está fusionada con otra mesa."
                );
            }

            table.setAnchorTableId(anchorTableId);
            table.setStatus(anchorTable.getStatus());
            restaurantTableRepository.save(table);
            actualMergedIds.add(tableId);
        }

        if (!actualMergedIds.isEmpty()) {
            eventPublisher.publishEvent(new TablesMergedEvent(anchorTableId, actualMergedIds, waiterUsername));
            log.info("Mesas {} fusionadas exitosamente bajo la mesa ancla {}.", actualMergedIds, anchorTableId);
        }
    }

    @Override
    public void unmergeTables(Long anchorTableId, String waiterUsername) {
        RestaurantTable anchorTable = restaurantTableRepository.findById(anchorTableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa ancla no encontrada con ID: " + anchorTableId));

        if (anchorTable.getStatus() != TableStatus.FREE && anchorTable.getStatus() != TableStatus.UNATTENDED) {
            throw new BusinessRuleViolationException(
                    "TABLE_OCCUPIED",
                    "No se puede deshacer la fusión porque la mesa ancla tiene un pedido activo en curso (estado actual: " + anchorTable.getStatus() + ")."
            );
        }

        List<RestaurantTable> merged = restaurantTableRepository.findAllByAnchorTableId(anchorTableId);
        List<Long> unmergedIds = new ArrayList<>();
        for (RestaurantTable table : merged) {
            table.setAnchorTableId(null);
            table.setStatus(TableStatus.FREE);
            restaurantTableRepository.save(table);
            unmergedIds.add(table.getId());
        }

        if (!unmergedIds.isEmpty()) {
            eventPublisher.publishEvent(new TablesUnmergedEvent(anchorTableId, unmergedIds, waiterUsername));
            log.info("Fusión deshecha. Mesas {} liberadas de la mesa ancla {}.", unmergedIds, anchorTableId);
        }
    }

    @Override
    public void transferOrder(Long fromTableId, Long toTableId, String waiterUsername) {
        RestaurantTable fromTable = restaurantTableRepository.findById(fromTableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa de origen no encontrada con ID: " + fromTableId));

        List<RestaurantTable> activeMerges = restaurantTableRepository.findAllByAnchorTableId(fromTableId);
        if (!activeMerges.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "TABLE_MERGED",
                    "No se permite trasladar el pedido de una mesa ancla con fusiones activas. Debe deshacer la fusión primero."
            );
        }

        Order order = orderRepository.findByTableIdAndStatusNot(fromTableId, OrderStatus.FREE)
                .filter(o -> o.getStatus() != OrderStatus.PAID)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "No se encontró comanda activa para la mesa de origen."));

        RestaurantTable toTable = restaurantTableRepository.findById(toTableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Mesa de destino no encontrada con ID: " + toTableId));

        if (toTable.getStatus() != TableStatus.FREE || toTable.getAnchorTableId() != null) {
            throw new com.pezbackend.orders.domain.exceptions.TableNotAvailableException(
                    "La mesa de destino con número " + toTable.getNumber() + " no está disponible."
            );
        }

        TableStatus currentStatus = fromTable.getStatus();

        // Trasladar comanda
        order.setTableId(toTableId);
        orderRepository.save(order);

        // Actualizar estados
        toTable.setStatus(currentStatus);
        syncMergedTablesStatus(toTableId, currentStatus);
        restaurantTableRepository.save(toTable);

        fromTable.setStatus(TableStatus.FREE);
        restaurantTableRepository.save(fromTable);

        log.info("Comanda {} trasladada exitosamente de mesa {} a mesa {}.", order.getId(), fromTable.getNumber(), toTable.getNumber());
        eventPublisher.publishEvent(new OrderTransferredEvent(order.getId(), fromTableId, toTableId, waiterUsername));
    }

    private void syncMergedTablesStatus(Long anchorId, TableStatus status) {
        List<RestaurantTable> mergedTables = restaurantTableRepository.findAllByAnchorTableId(anchorId);
        for (RestaurantTable mt : mergedTables) {
            mt.setStatus(status);
            restaurantTableRepository.save(mt);
        }
    }

    private void releaseMergedTables(Long anchorId) {
        List<RestaurantTable> mergedTables = restaurantTableRepository.findAllByAnchorTableId(anchorId);
        for (RestaurantTable mt : mergedTables) {
            mt.setStatus(TableStatus.FREE);
            mt.setAnchorTableId(null);
            restaurantTableRepository.save(mt);
            eventPublisher.publishEvent(new TableReleasedEvent(mt.getId(), mt.getNumber()));
        }
    }
}
