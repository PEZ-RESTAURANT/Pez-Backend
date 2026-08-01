package com.pezbackend.orders;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.model.valueobjects.*;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.interfaces.rest.resources.PriceAdjustmentResource;
import com.pezbackend.orders.interfaces.rest.transform.PriceAdjustmentResourceFromEntityAssembler;
import com.pezbackend.shared.domain.exceptions.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de integración para validar el ciclo de vida y la máquina de estados
 * del Bounded Context de Orders, incluyendo colas FIFO, ajustes de precio
 * y la trazabilidad de mesa compartida por múltiples mozos.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrdersIntegrationTests {

    @Autowired
    private OrderCommandService commandService;

    @Autowired
    private OrderQueryService queryService;

    @Autowired
    private ProductRepository productRepository;

    private Product ceviche;

    @BeforeEach
    public void setUp() {
        ceviche = new Product("Ceviche Carretillero", new BigDecimal("35.00"), ProductCategory.MARINA);
        ceviche = productRepository.save(ceviche);
    }

    @Test
    public void testStateMachineFlowAndTransitions() {
        // 1. Crear una mesa física
        RestaurantTable table = commandService.createTable(10, 1, "Zona Central", 100, 100);
        final Long targetTableId = table.getId();
        assertThat(table.getStatus()).isEqualTo(TableStatus.FREE);

        // 2. Solicitar atención (FREE -> UNATTENDED)
        commandService.requestAttention(targetTableId);
        table = queryService.getTableById(targetTableId);
        assertThat(table.getStatus()).isEqualTo(TableStatus.UNATTENDED);

        // Verificar que la comanda activa se crea en estado UNATTENDED
        List<Order> orders = queryService.getAllOrders();
        Order activeOrder = orders.stream()
                .filter(o -> o.getTableId() != null && o.getTableId().equals(targetTableId))
                .findFirst().orElseThrow();
        assertThat(activeOrder.getStatus()).isEqualTo(OrderStatus.UNATTENDED);

        // 3. Mozo atiende la mesa (UNATTENDED -> TAKING_ORDER)
        commandService.attendTable(table.getId(), "waiter_user");
        table = queryService.getTableById(table.getId());
        activeOrder = queryService.getOrderById(activeOrder.getId());
        assertThat(table.getStatus()).isEqualTo(TableStatus.TAKING_ORDER);
        assertThat(activeOrder.getStatus()).isEqualTo(OrderStatus.TAKING_ORDER);

        // 4. Agregar plato a la comanda (TAKING_ORDER -> WAITING_DISHES)
        commandService.addItemsToOrder(activeOrder.getId(), ceviche.getId(), 2, "Sin picante", 1L, "waiter_user");
        activeOrder = queryService.getOrderById(activeOrder.getId());
        table = queryService.getTableById(table.getId());
        assertThat(activeOrder.getStatus()).isEqualTo(OrderStatus.WAITING_DISHES);
        assertThat(table.getStatus()).isEqualTo(TableStatus.WAITING_DISHES);
        assertThat(activeOrder.getItems()).hasSize(1);
        OrderItem item = activeOrder.getItems().get(0);
        assertThat(item.getStatus()).isEqualTo(OrderItemStatus.PENDING);

        // 5. Cocina empieza preparación y la termina (READY)
        commandService.changeItemStatus(activeOrder.getId(), item.getId(), "READY", "cook_user");
        activeOrder = queryService.getOrderById(activeOrder.getId());
        item = activeOrder.getItems().get(0);
        assertThat(item.getStatus()).isEqualTo(OrderItemStatus.READY);
        assertThat(item.getReadyAt()).isNotNull();

        // 6. Mozo entrega el plato (DELIVERED -> ALL_DELIVERED automático)
        commandService.changeItemStatus(activeOrder.getId(), item.getId(), "DELIVERED", "waiter_user");
        activeOrder = queryService.getOrderById(activeOrder.getId());
        table = queryService.getTableById(table.getId());
        item = activeOrder.getItems().get(0);
        assertThat(item.getStatus()).isEqualTo(OrderItemStatus.DELIVERED);
        assertThat(item.getDeliveredAt()).isNotNull();
        assertThat(activeOrder.getStatus()).isEqualTo(OrderStatus.ALL_DELIVERED);
        assertThat(table.getStatus()).isEqualTo(TableStatus.ALL_DELIVERED);

        // 7. Emisión de comprobante precuenta (ALL_DELIVERED -> ISSUED_UNPAID)
        commandService.issueReceipt(activeOrder.getId(), "cashier_user");
        activeOrder = queryService.getOrderById(activeOrder.getId());
        table = queryService.getTableById(table.getId());
        assertThat(activeOrder.getStatus()).isEqualTo(OrderStatus.ISSUED_UNPAID);
        assertThat(table.getStatus()).isEqualTo(TableStatus.ISSUED_UNPAID);

        // 8. Pago de la cuenta (ISSUED_UNPAID -> PAID -> FREE automático)
        commandService.markAsPaid(activeOrder.getId());
        activeOrder = queryService.getOrderById(activeOrder.getId());
        table = queryService.getTableById(table.getId());
        assertThat(activeOrder.getStatus()).isEqualTo(OrderStatus.FREE);
        assertThat(table.getStatus()).isEqualTo(TableStatus.FREE);
    }

    @Test
    public void testInvalidTransitionsThrowExceptions() {
        RestaurantTable table = commandService.createTable(11, 1, "Zona Central", 100, 100);
        
        // No se puede atender una mesa que está FREE (debe solicitar atención primero)
        assertThatThrownBy(() -> commandService.attendTable(table.getId(), "waiter_user"))
                .isInstanceOf(InvalidStateTransitionException.class);

        commandService.requestAttention(table.getId());

        // No se puede solicitar atención en una mesa que ya está esperando (UNATTENDED)
        assertThatThrownBy(() -> commandService.requestAttention(table.getId()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    public void testAttentionAndKitchenQueuesFIFO() throws InterruptedException {
        RestaurantTable t1 = commandService.createTable(21, 1, "Central", 10, 10);
        RestaurantTable t2 = commandService.createTable(22, 1, "Central", 20, 20);

        // t2 solicita atención primero
        commandService.requestAttention(t2.getId());
        Thread.sleep(50); // Garantizar delta de tiempo
        commandService.requestAttention(t1.getId());

        // La cola de atención debe devolver t2 antes que t1 (FIFO)
        List<RestaurantTable> attentionQueue = queryService.getAttentionQueue();
        assertThat(attentionQueue).hasSize(2);
        assertThat(attentionQueue.get(0).getId()).isEqualTo(t2.getId());
        assertThat(attentionQueue.get(1).getId()).isEqualTo(t1.getId());

        // Atender mesas
        commandService.attendTable(t2.getId(), "waiter");
        commandService.attendTable(t1.getId(), "waiter");

        Order order2 = queryService.getAllOrders().stream()
                .filter(o -> o.getTableId() != null && o.getTableId().equals(t2.getId())).findFirst().orElseThrow();
        Order order1 = queryService.getAllOrders().stream()
                .filter(o -> o.getTableId() != null && o.getTableId().equals(t1.getId())).findFirst().orElseThrow();

        // Encolar ítems en cocina
        commandService.addItemsToOrder(order1.getId(), ceviche.getId(), 1, "Normal", 1L, "waiter");
        Thread.sleep(50);
        commandService.addItemsToOrder(order2.getId(), ceviche.getId(), 2, "Picante", 1L, "waiter");

        // La cola de cocina debe devolver el plato de order1 antes que el de order2 (FIFO)
        List<OrderItem> kitchenQueue = queryService.getKitchenQueue(null);
        assertThat(kitchenQueue).hasSize(2);
        assertThat(kitchenQueue.get(0).getOrderId()).isEqualTo(order1.getId());
        assertThat(kitchenQueue.get(1).getOrderId()).isEqualTo(order2.getId());
    }

    @Test
    public void testPriceAdjustments() {
        RestaurantTable table = commandService.createTable(30, 1, "Central", 50, 50);
        Order order = commandService.createOrder(table.getId(), "DINE_IN", 100L);

        commandService.applyPriceAdjustment(
                order.getId(),
                "ALL",
                "PERMANENT",
                null,
                null,
                new BigDecimal("5.00"),
                "Descuento de cortesía",
                "admin_user"
        );

        Order updated = queryService.getOrderById(order.getId());
        assertThat(updated.getPriceAdjustments()).hasSize(1);
        PriceAdjustmentResource adj = PriceAdjustmentResourceFromEntityAssembler
                .toResourceFromEntity(updated.getPriceAdjustments().get(0));
        assertThat(adj.reason()).isEqualTo("Descuento de cortesía");
        assertThat(adj.newValue()).isEqualByComparingTo("5.00");
    }

    @Test
    public void testMultipleWaitersOnSameTable() {
        RestaurantTable table = commandService.createTable(40, 1, "Mesa Compartida", 80, 80);
        Order order = commandService.createOrder(table.getId(), "DINE_IN", null);

        // Mozo 101 agrega un plato a la mesa
        commandService.addItemsToOrder(order.getId(), ceviche.getId(), 1, "Sin sal", 101L, "waiter_101");
        
        // Mozo 102 agrega otro plato a la misma mesa/pedido de forma paralela
        commandService.addItemsToOrder(order.getId(), ceviche.getId(), 2, "Con yuyo", 102L, "waiter_102");

        // Verificar que cada ítem registre su propio waiterId correctamente para trazabilidad
        Order finalOrder = queryService.getOrderById(order.getId());
        assertThat(finalOrder.getItems()).hasSize(2);
        
        OrderItem item1 = finalOrder.getItems().stream()
                .filter(i -> i.getWaiterId().equals(101L))
                .findFirst().orElseThrow();
        assertThat(item1.getQuantity()).isEqualTo(1);
        assertThat(item1.getNote()).isEqualTo("Sin sal");

        OrderItem item2 = finalOrder.getItems().stream()
                .filter(i -> i.getWaiterId().equals(102L))
                .findFirst().orElseThrow();
        assertThat(item2.getQuantity()).isEqualTo(2);
        assertThat(item2.getNote()).isEqualTo("Con yuyo");
    }
}
