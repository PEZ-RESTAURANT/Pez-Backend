package com.pezbackend.inventory;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import com.pezbackend.catalog.domain.services.RecipeCommandService;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.RecipeRepository;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.model.valueobjects.StockMovementType;
import com.pezbackend.inventory.domain.services.SupplyCommandService;
import com.pezbackend.inventory.domain.services.SupplyQueryService;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.StockMovementRepository;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.valueobjects.OrderItemStatus;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.shared.domain.model.DomainEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplyRepository supplyRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeCommandService recipeCommandService;

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository orderRepository;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductKitchenZoneRepository productKitchenZoneRepository;

    @Autowired
    private EventCollector eventCollector;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        public EventCollector eventCollector() {
            return new EventCollector();
        }
    }

    static class EventCollector {
        private final List<DomainEvent> events = new ArrayList<>();

        @org.springframework.context.event.EventListener
        public void onEvent(DomainEvent event) {
            events.add(event);
        }

        public List<DomainEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }

    private User adminUser;
    private User cookUser;
    private UserDetailsImpl adminDetails;
    private UserDetailsImpl cookDetails;

    private Supply supplyCarne;
    private Supply supplyLimon;
    private Product productLomo;

    @BeforeEach
    public void setUp() {
        // Limpiar base de datos en orden de dependencias
        auditEventRepository.deleteAll();
        stockMovementRepository.deleteAll();
        recipeRepository.deleteAll();
        productKitchenZoneRepository.deleteAll();
        orderRepository.deleteAll();
        restaurantTableRepository.deleteAll();
        productRepository.deleteAll();
        supplyRepository.deleteAll();

        // Asegurar roles
        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));
        Role cookRole = roleRepository.findByName(Roles.COOK)
                .orElseGet(() -> roleRepository.save(new Role(Roles.COOK)));

        userRepository.findByEmail("admin@pez.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("cook@pez.com").ifPresent(userRepository::delete);

        adminUser = new User("admin@pez.com", "pw", "Admin", "User", true);
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminDetails = UserDetailsImpl.build(adminUser);

        cookUser = new User("cook@pez.com", "pw", "Cook", "User", true);
        cookUser.addRole(cookRole);
        cookUser = userRepository.save(cookUser);
        cookDetails = UserDetailsImpl.build(cookUser);

        // Crear insumos
        supplyCarne = new Supply("Lomo Fino", "kg", new BigDecimal("2.0000"));
        supplyCarne.setCurrentStock(new BigDecimal("10.0000"));
        supplyCarne = supplyRepository.save(supplyCarne);

        supplyLimon = new Supply("Limon Criollo", "kg", new BigDecimal("1.0000"));
        supplyLimon.setCurrentStock(new BigDecimal("0.5000")); // Ya empieza bajo el umbral (0.5 < 1.0)
        supplyLimon = supplyRepository.save(supplyLimon);

        // Crear producto
        productLomo = new Product("Lomo Saltado Premium", new BigDecimal("45.00"), ProductCategory.MARINA);
        productLomo = productRepository.save(productLomo);

        // Asignar receta: 1 Lomo Saltado usa 0.2500 kg de Lomo Fino y 0.1000 kg de Limon
        recipeCommandService.addOrUpdateRecipeItem(productLomo.getId(), supplyCarne.getId(), new BigDecimal("0.2500"));
        recipeCommandService.addOrUpdateRecipeItem(productLomo.getId(), supplyLimon.getId(), new BigDecimal("0.1000"));
    }

    @Test
    public void testAutomaticStockDeductionOnDelivery() throws Exception {
        // Crear comanda
        RestaurantTable table = orderCommandService.createTable(99, 1, "Salón Principal", 4, 4);
        orderCommandService.requestAttention(table.getId());
        orderCommandService.attendTable(table.getId(), "waiter_user");

        Order order = orderQueryService.getAllOrders().stream()
                .filter(o -> o.getTableId() != null && o.getTableId().equals(table.getId()))
                .findFirst().orElseThrow();

        // Agregar Lomo Saltado (cantidad = 2)
        orderCommandService.addItemsToOrder(order.getId(), productLomo.getId(), 2, "Término medio", 1L, "waiter_user");

        Order savedOrder = orderQueryService.getOrderById(order.getId());
        OrderItem item = savedOrder.getItems().stream()
                .filter(i -> i.getProductId().equals(productLomo.getId()))
                .findFirst().orElseThrow();

        // Limpiar colector de eventos para capturar solo lo que ocurra durante la entrega
        eventCollector.clear();

        // Marcar plato como entregado (DELIVERED) vía endpoint REST de orders (modifica el estado y gatilla el evento)
        mockMvc.perform(post("/api/v1/orders/" + order.getId() + "/items/" + item.getId() + "/status")
                        .param("status", "DELIVERED")
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        // El descuento de stock ocurre de forma desacoplada y asíncrona mediante el TransactionalEventListener (en el mismo hilo del commit).
        // Esperamos que se hayan descontado:
        // - Lomo Fino: 0.2500 kg * 2 = 0.5000 kg. Nuevo Stock: 10.0000 - 0.5000 = 9.5000 kg.
        // - Limon Criollo: 0.1000 kg * 2 = 0.2000 kg. Nuevo Stock: 0.5000 - 0.2000 = 0.3000 kg.
        Supply finalCarne = supplyRepository.findById(supplyCarne.getId()).orElseThrow();
        assertThat(finalCarne.getCurrentStock()).isEqualByComparingTo("9.5000");

        Supply finalLimon = supplyRepository.findById(supplyLimon.getId()).orElseThrow();
        assertThat(finalLimon.getCurrentStock()).isEqualByComparingTo("0.3000");

        // Verificar registro de movimientos de stock tipo SALE_DEDUCTION
        List<StockMovement> movementsCarne = stockMovementRepository.findAllBySupplyIdOrderByDateDesc(supplyCarne.getId());
        assertThat(movementsCarne).hasSize(1);
        assertThat(movementsCarne.get(0).getType()).isEqualTo(StockMovementType.SALE_DEDUCTION);
        assertThat(movementsCarne.get(0).getQuantity()).isEqualByComparingTo("0.5000");

        List<StockMovement> movementsLimon = stockMovementRepository.findAllBySupplyIdOrderByDateDesc(supplyLimon.getId());
        assertThat(movementsLimon).hasSize(1);
        assertThat(movementsLimon.get(0).getType()).isEqualTo(StockMovementType.SALE_DEDUCTION);
        assertThat(movementsLimon.get(0).getQuantity()).isEqualByComparingTo("0.2000");

        // Verificar publicación de eventos de inventario
        List<DomainEvent> events = eventCollector.getEvents();
        
        // Debe haberse publicado SupplyDeductedBySale para ambos insumos
        boolean carneDeductedEvent = events.stream()
                .anyMatch(e -> "SupplyDeductedBySale".equals(e.eventType()) && e.payload().toString().contains("supplyId=" + supplyCarne.getId()));
        boolean limonDeductedEvent = events.stream()
                .anyMatch(e -> "SupplyDeductedBySale".equals(e.eventType()) && e.payload().toString().contains("supplyId=" + supplyLimon.getId()));
        
        assertThat(carneDeductedEvent).isTrue();
        assertThat(limonDeductedEvent).isTrue();
    }

    @Test
    public void testDeductionWithInsufficientStockDoesNotBlockAndTriggersMismatch() throws Exception {
        // Establecer el stock de Limón en cero
        supplyLimon.setCurrentStock(BigDecimal.ZERO);
        supplyRepository.save(supplyLimon);

        RestaurantTable table = orderCommandService.createTable(100, 1, "Salón Principal", 4, 4);
        orderCommandService.requestAttention(table.getId());
        orderCommandService.attendTable(table.getId(), "waiter_user");

        Order order = orderQueryService.getAllOrders().stream()
                .filter(o -> o.getTableId() != null && o.getTableId().equals(table.getId()))
                .findFirst().orElseThrow();

        // Agregar Lomo Saltado (cantidad = 1)
        orderCommandService.addItemsToOrder(order.getId(), productLomo.getId(), 1, "Término medio", 1L, "waiter_user");

        Order savedOrder = orderQueryService.getOrderById(order.getId());
        OrderItem item = savedOrder.getItems().stream()
                .filter(i -> i.getProductId().equals(productLomo.getId()))
                .findFirst().orElseThrow();

        eventCollector.clear();

        // Marcar plato como entregado
        mockMvc.perform(post("/api/v1/orders/" + order.getId() + "/items/" + item.getId() + "/status")
                        .param("status", "DELIVERED")
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        // La operación de entrega completó con éxito (no bloqueó)
        Order afterDelivery = orderQueryService.getOrderById(order.getId());
        OrderItem itemAfterDelivery = afterDelivery.getItems().stream()
                .filter(i -> i.getId().equals(item.getId()))
                .findFirst().orElseThrow();
        assertThat(itemAfterDelivery.getStatus()).isEqualTo(OrderItemStatus.DELIVERED);

        // El stock de limón se redujo a negativo: 0 - 0.1000 = -0.1000
        Supply finalLimon = supplyRepository.findById(supplyLimon.getId()).orElseThrow();
        assertThat(finalLimon.getCurrentStock()).isEqualByComparingTo("-0.1000");

        // Debe haberse publicado el evento StockMismatchDetected indicando desajuste de stock
        List<DomainEvent> events = eventCollector.getEvents();
        boolean mismatchEvent = events.stream()
                .anyMatch(e -> "StockMismatchDetected".equals(e.eventType()) && e.payload().toString().contains("supplyId=" + supplyLimon.getId()));
        assertThat(mismatchEvent).isTrue();
    }

    @Test
    public void testManualAdjustmentValidationAndRejection() throws Exception {
        // 1. Intentar ajuste manual sin proporcionar justificación (reason) -> Debe fallar o ser rechazado por regla de negocio (409 Conflict)
        mockMvc.perform(post("/api/v1/inventory/supplies/" + supplyCarne.getId() + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": -1.0000, \"reason\": \"\"}")
                        .with(user(adminDetails)))
                .andExpect(status().isConflict()); 
    }

    @Test
    public void testLowStockAlertIsTriggeredOnAdjustment() throws Exception {
        eventCollector.clear();

        // Ajustar Lomo Fino a una cantidad que cruza por debajo de minThreshold (10 -> 1.5000, cruza minThreshold de 2.0000)
        // Restar 8.5 kg
        mockMvc.perform(post("/api/v1/inventory/supplies/" + supplyCarne.getId() + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": -8.5000, \"reason\": \"Mermas por refrigeración fallida\"}")
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // Validar nuevo stock de carne = 10 - 8.5 = 1.5 kg
        Supply finalCarne = supplyRepository.findById(supplyCarne.getId()).orElseThrow();
        assertThat(finalCarne.getCurrentStock()).isEqualByComparingTo("1.5000");

        // Verificar emisión de LowStockAlertTriggered
        List<DomainEvent> events = eventCollector.getEvents();
        boolean lowStockEvent = events.stream()
                .anyMatch(e -> "LowStockAlertTriggered".equals(e.eventType()) && e.payload().toString().contains("supplyId=" + supplyCarne.getId()));
        assertThat(lowStockEvent).isTrue();
    }
}
