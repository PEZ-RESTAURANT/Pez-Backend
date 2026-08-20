package com.pezbackend.kitchen;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.catalog.domain.services.ProductKitchenZoneCommandService;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.domain.services.KitchenZoneCommandService;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.model.valueobjects.OrderItemStatus;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para validar las operaciones y fronteras del Bounded Context Kitchen (Fase 3).
 * Se valida el filtrado por zonas en cocina, el inicio y fin de preparación y su impacto auditado en orders.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class KitchenIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KitchenZoneCommandService kitchenZoneCommandService;

    @Autowired
    private ProductKitchenZoneCommandService productKitchenZoneCommandService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
    private com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductKitchenZoneRepository productKitchenZoneRepository;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository orderRepository;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.KitchenZoneRepository kitchenZoneRepository;

    @Autowired
    private EventCollector eventCollector;

    @Autowired
    private com.pezbackend.shared.infrastructure.DomainEventListener domainEventListener;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public EventCollector eventCollector() {
            return new EventCollector();
        }
    }

    static class EventCollector {
        private final List<com.pezbackend.shared.domain.model.DomainEvent> events = new java.util.ArrayList<>();

        @org.springframework.context.event.EventListener
        public void onEvent(com.pezbackend.shared.domain.model.DomainEvent event) {
            events.add(event);
        }

        public List<com.pezbackend.shared.domain.model.DomainEvent> getEvents() {
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

    private KitchenZone zoneA;
    private KitchenZone zoneB;
    private Product productX;
    private Product productY;

    @BeforeEach
    public void setUp() {
        // Limpiar base de datos en orden de dependencias para evitar violaciones de clave foránea
        auditEventRepository.deleteAll();
        productKitchenZoneRepository.deleteAll();
        orderRepository.deleteAll();
        restaurantTableRepository.deleteAll();
        productRepository.deleteAll();
        kitchenZoneRepository.deleteAll();

        // Garantizar existencia de roles
        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));
        Role cookRole = roleRepository.findByName(Roles.COOK)
                .orElseGet(() -> roleRepository.save(new Role(Roles.COOK)));

        // Buscar o crear usuarios de prueba con roles específicos
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

        // Crear zonas de cocina
        zoneA = kitchenZoneCommandService.createZone("Cocina Caliente", false);
        zoneB = kitchenZoneCommandService.createZone("Bar", false);

        // Crear productos
        Category marina = categoryRepository.save(new Category("Marina"));
        Category bebidas = categoryRepository.save(new Category("Bebidas"));
        
        productX = new Product("Ceviche Clásico", new BigDecimal("38.00"), marina);
        productX = productRepository.save(productX);

        productY = new Product("Pisco Sour", new BigDecimal("22.00"), bebidas);
        productY = productRepository.save(productY);

        // Asignar productos a zonas de cocina
        productKitchenZoneCommandService.assignProductToZone(productX.getId(), zoneA.getId());
        productKitchenZoneCommandService.assignProductToZone(productY.getId(), zoneB.getId());
    }

    @Test
    public void testProductZoneAssignmentEndpoint() throws Exception {
        // Asignar productY a zoneA
        mockMvc.perform(put("/api/v1/products/" + productY.getId() + "/kitchen-zone")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zoneId\": " + zoneA.getId() + "}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testKitchenQueueAndOperations() throws Exception {
        // Crear comanda en salón
        RestaurantTable table = orderCommandService.createTable(88, 1, "Salón", 5, 5);
        orderCommandService.requestAttention(table.getId());
        orderCommandService.attendTable(table.getId(), "waiter_username");

        Order order = orderQueryService.getAllOrders().stream()
                .filter(o -> o.getTableId() != null && o.getTableId().equals(table.getId()))
                .findFirst().orElseThrow();

        // Agregar platos
        orderCommandService.addItemsToOrder(order.getId(), productX.getId(), 1, "Sin ají", 2L, "waiter");
        orderCommandService.addItemsToOrder(order.getId(), productY.getId(), 2, "Catedral", 2L, "waiter");

        // Obtener cola para zona A (Cocina Caliente) -> Debe tener Ceviche pero NO Pisco Sour
        mockMvc.perform(get("/api/v1/kitchen/zones/" + zoneA.getId() + "/queue")
                        .with(user(cookDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(productX.getId()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // Obtener cola para zona B (Bar) -> Debe tener Pisco Sour pero NO Ceviche
        mockMvc.perform(get("/api/v1/kitchen/zones/" + zoneB.getId() + "/queue")
                        .with(user(cookDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(productY.getId()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // Recuperar ID de ítem Ceviche
        Order finalOrder = orderQueryService.getOrderById(order.getId());
        OrderItem cevicheItem = finalOrder.getItems().stream()
                .filter(i -> i.getProductId().equals(productX.getId()))
                .findFirst().orElseThrow();

        // Limpiar colector de eventos y base de datos de auditoría
        eventCollector.clear();
        auditEventRepository.deleteAll();

        // Iniciar preparación de plato a través de MockMvc
        mockMvc.perform(post("/api/v1/kitchen/items/" + cevicheItem.getId() + "/start-preparation")
                        .with(user(cookDetails)))
                .andExpect(status().isNoContent());

        // Verificar cambio de estado a IN_PREPARATION
        Order afterStart = orderQueryService.getOrderById(order.getId());
        OrderItem cevicheAfterStart = afterStart.getItems().stream()
                .filter(i -> i.getId().equals(cevicheItem.getId()))
                .findFirst().orElseThrow();
        assertThat(cevicheAfterStart.getStatus()).isEqualTo(OrderItemStatus.IN_PREPARATION);

        // Verificar que el evento de dominio correcto fue publicado
        List<com.pezbackend.shared.domain.model.DomainEvent> publishedEvents = eventCollector.getEvents();
        assertThat(publishedEvents).hasSize(1);
        com.pezbackend.shared.domain.model.DomainEvent event = publishedEvents.get(0);
        assertThat(event.eventType()).isEqualTo("ItemStatusChanged");
        assertThat(event.module()).isEqualTo("orders");
        assertThat(event.userId()).isEqualTo("cook@pez.com");

        // Invocar directamente el listener con el evento capturado para verificar persistencia en la base de datos de auditoría
        domainEventListener.handleDomainEvent(event);

        List<AuditEvent> auditEvents = auditEventRepository.findAll();
        assertThat(auditEvents).hasSize(1);
        AuditEvent audit = auditEvents.get(0);
        assertThat(audit.getEventType()).isEqualTo("ItemStatusChanged");
        assertThat(audit.getModule()).isEqualTo("orders");
        assertThat(audit.getUserId()).isEqualTo("cook@pez.com");

        // Terminar preparación de plato vía MockMvc para validar mapeos HTTP y seguridad del controlador
        mockMvc.perform(post("/api/v1/kitchen/items/" + cevicheItem.getId() + "/mark-ready")
                        .with(user(cookDetails)))
                .andExpect(status().isNoContent());

        // Verificar cambio de estado a READY y seteo de hora de fin
        Order afterReady = orderQueryService.getOrderById(order.getId());
        OrderItem cevicheAfterReady = afterReady.getItems().stream()
                .filter(i -> i.getId().equals(cevicheItem.getId()))
                .findFirst().orElseThrow();
        assertThat(cevicheAfterReady.getStatus()).isEqualTo(OrderItemStatus.READY);
        assertThat(cevicheAfterReady.getReadyAt()).isNotNull();
    }
}
