package com.pezbackend.billing;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.valueobjects.SaleStatus;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashRegisterMismatch;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementReason;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterMismatchRepository;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.cashregister.application.internal.services.CashRegisterScheduler;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.valueobjects.OrderStatus;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.valueobjects.OrderItemStatus;
import com.pezbackend.orders.domain.model.valueobjects.TableStatus;
import com.pezbackend.shared.domain.model.DomainEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BillingCashRegisterIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private CashRegisterRepository cashRegisterRepository;

    @Autowired
    private CashRegisterMismatchRepository cashRegisterMismatchRepository;

    @Autowired
    private CashRegisterCommandService cashRegisterCommandService;

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
    private com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.RestaurantRepository restaurantRepository;

    private com.pezbackend.tenancy.domain.model.aggregates.Restaurant testRestaurant;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository orderRepository;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private CashRegisterScheduler cashRegisterScheduler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

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
    private UserDetailsImpl adminDetails;
    private Product productCeviche;

    @BeforeEach
    public void setUp() {
        // Limpiar base de datos
        auditEventRepository.deleteAll();
        saleRepository.deleteAll();
        cashRegisterMismatchRepository.deleteAll();
        cashRegisterRepository.deleteAll();
        orderRepository.deleteAll();
        restaurantTableRepository.deleteAll();
        productRepository.deleteAll();

        // Crear restaurant para pruebas
        testRestaurant = new com.pezbackend.tenancy.domain.model.aggregates.Restaurant("Test Restaurant", "12345678901", "test@res.com", "999888777", "123 Test St");
        testRestaurant = restaurantRepository.save(testRestaurant);

        // Sembrar roles y usuarios de seguridad
        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));

        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id IN (SELECT id FROM user WHERE email = ?)", "admin@pez.com");
        jdbcTemplate.update("DELETE FROM user WHERE email = ?", "admin@pez.com");

        adminUser = new User("admin@pez.com", "pw", "Admin", "User", true);
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);

        adminDetails = UserDetailsImpl.build(adminUser);

        // Crear producto de prueba con categoría dinámica
        Category marina = categoryRepository.save(new Category("Marina"));
        productCeviche = new Product("Ceviche de Prueba", new BigDecimal("40.00"), marina);
        productCeviche = productRepository.save(productCeviche);
    }

    @Test
    public void testFullOrderToSaleAndPaymentFlow() throws Exception {
        // 1. Abrir caja registradora
        mockMvc.perform(post("/api/v1/cash-registers/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openingBalance\": 100.00}")
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // 2. Crear comanda
        RestaurantTable table = orderCommandService.createTable(5, 1, "Salón", 4, 4);
        orderCommandService.requestAttention(table.getId());
        orderCommandService.attendTable(table.getId(), "waiter");

        Order order = orderQueryService.getAllOrders().stream()
                .filter(o -> o.getTableId() != null && o.getTableId().equals(table.getId()))
                .findFirst().orElseThrow();

        // Agregar ceviche (cantidad = 2 -> total = 80.00)
        orderCommandService.addItemsToOrder(order.getId(), productCeviche.getId(), 2, "Sin picante", 1L, "waiter");

        Order savedOrder = orderQueryService.getOrderById(order.getId());
        OrderItem item = savedOrder.getItems().get(0);

        // Transition item status to DELIVERED (order status will become ALL_DELIVERED)
        mockMvc.perform(post("/api/v1/orders/" + order.getId() + "/items/" + item.getId() + "/status")
                        .param("status", "DELIVERED")
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        Order deliveredOrder = orderQueryService.getOrderById(order.getId());
        assertThat(deliveredOrder.getStatus()).isEqualTo(OrderStatus.ALL_DELIVERED);

        // 3. Emitir comprobante de venta (POST /api/v1/sales)
        String createSaleJson = String.format(
                "{\"orderId\": %d, \"documentType\": \"BOLETA\", \"customerDocumentNumber\": \"12345678\"}",
                order.getId()
        );

        String saleIdStr = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createSaleJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long saleId = Long.parseLong(saleIdStr);

        Sale sale = saleRepository.findById(saleId).orElseThrow();
        assertThat(sale.getSaleStatus()).isEqualTo(SaleStatus.ISSUED_UNPAID);
        assertThat(sale.getTotal()).isEqualByComparingTo("80.00");
        assertThat(sale.getCustomerDocumentNumber()).isEqualTo("12345678");

        // 4. Registrar pagos en efectivo
        eventCollector.clear();

        String registerPaymentsJson = "{\"payments\": [{\"method\": \"CASH\", \"amount\": 50.00}, {\"method\": \"CARD\", \"amount\": 30.00}]}";
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPaymentsJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // Validar transiciones posteriores al pago completo
        Sale paidSale = saleRepository.findById(saleId).orElseThrow();
        assertThat(paidSale.getSaleStatus()).isEqualTo(SaleStatus.PAID);

        Order paidOrder = orderQueryService.getOrderById(order.getId());
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.FREE); // Order is freed on PAID

        RestaurantTable freedTable = restaurantTableRepository.findById(table.getId()).orElseThrow();
        assertThat(freedTable.getStatus()).isEqualTo(TableStatus.FREE);

        // Verificar que el ingreso en efectivo de 50.00 se registró en la caja activa
        CashRegister cashRegister = cashRegisterRepository.findByStatus(CashRegisterStatus.OPEN).orElseThrow();
        assertThat(cashRegister.getCurrentBalance()).isEqualByComparingTo("150.00"); // 100 opening + 50 cash

        // Verificar eventos publicados
        assertThat(eventCollector.getEvents().stream()
                .anyMatch(e -> e.eventType().equals("SaleMarkedPaid"))).isTrue();
    }

    @Test
    public void testCashRegisterMismatchOnClose() throws Exception {
        // 1. Abrir caja registradora
        mockMvc.perform(post("/api/v1/cash-registers/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openingBalance\": 200.00}")
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        CashRegister cashRegister = cashRegisterRepository.findByStatus(CashRegisterStatus.OPEN).orElseThrow();

        // 2. Agregar movimiento manual de egreso para el arqueo (tipo EXPENSE)
        String addMovementJson = "{\"type\": \"EXPENSE\", \"amount\": 50.00, \"reason\": \"SUPPLIER_PAYMENT\", \"note\": \"Pago de verduras\"}";
        mockMvc.perform(post("/api/v1/cash-registers/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addMovementJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // Balance esperado: 200 - 50 = 150.00
        CashRegister updatedRegister = cashRegisterRepository.findById(cashRegister.getId()).orElseThrow();
        assertThat(updatedRegister.getCurrentBalance()).isEqualByComparingTo("150.00");

        // 3. Arqueo y cierre con declaración descuadrada (declared = 140.00)
        eventCollector.clear();

        String closeDeclarationJson = "{\"declaredAmount\": 140.00}";
        mockMvc.perform(post("/api/v1/cash-registers/" + cashRegister.getId() + "/close-with-declaration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closeDeclarationJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // Caja debe estar cerrada
        CashRegister closedRegister = cashRegisterRepository.findById(cashRegister.getId()).orElseThrow();
        assertThat(closedRegister.getStatus()).isEqualTo(CashRegisterStatus.CLOSED);

        // Debe existir descuadre
        List<CashRegisterMismatch> mismatches = cashRegisterMismatchRepository.findAll();
        assertThat(mismatches).hasSize(1);
        assertThat(mismatches.get(0).getStatus()).isEqualTo("MISMATCHED");
        assertThat(mismatches.get(0).getExpectedAmount()).isEqualByComparingTo("150.00");
        assertThat(mismatches.get(0).getDeclaredAmount()).isEqualByComparingTo("140.00");
        assertThat(mismatches.get(0).getNotifiedAdminAt()).isNotNull();

        // Verificar evento publicado
        assertThat(eventCollector.getEvents().stream()
                .anyMatch(e -> e.eventType().equals("CashRegisterMismatched"))).isTrue();
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void testForcedCloseBySchedulerWithoutMovements() {
        com.pezbackend.shared.infrastructure.TenantContext.setCurrentTenantId(testRestaurant.getId());
        try {
            // 1. Crear caja registradora directamente en base de datos
            CashRegister oldRegister = new CashRegister(new BigDecimal("300.00"));
            oldRegister = cashRegisterRepository.save(oldRegister);

            // Utilizar JdbcTemplate para cambiar el createdAt a ayer (evitando auditoría de JPA)
            jdbcTemplate.update("UPDATE cash_register SET created_at = ? WHERE id = ?",
                    java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
                    oldRegister.getId());

            // Limpiar el contexto de persistencia de JPA para que el scheduler reciba el createdAt actualizado desde la BD
            entityManager.clear();

            // Configurar por reflexión el cutoff para que sea menor a la hora actual del test (ej: 5 minutos antes)
            try {
                LocalDateTime testNow = LocalDateTime.now();
                int h = testNow.getHour();
                int m = testNow.getMinute() - 5;
                if (m < 0) {
                    if (h > 0) {
                        h--;
                        m = 55;
                    } else {
                        h = 0;
                        m = 0;
                    }
                }

                java.lang.reflect.Field hourField = CashRegisterScheduler.class.getDeclaredField("cutoffHour");
                hourField.setAccessible(true);
                hourField.set(cashRegisterScheduler, h);

                java.lang.reflect.Field minuteField = CashRegisterScheduler.class.getDeclaredField("cutoffMinute");
                minuteField.setAccessible(true);
                minuteField.set(cashRegisterScheduler, m);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 2. Invocar manualmente el método del scheduler
            eventCollector.clear();
            cashRegisterScheduler.closeExpiredCashRegisters();

            // 3. Validar que se cerró automáticamente sin movimientos agregados
            CashRegister closedRegister = cashRegisterRepository.findById(oldRegister.getId()).orElseThrow();
            assertThat(closedRegister.getStatus()).isEqualTo(CashRegisterStatus.CLOSED);
            assertThat(closedRegister.getCurrentBalance()).isEqualByComparingTo("300.00");
            assertThat(closedRegister.getMovements()).isEmpty(); // No dummy movements added!

            // Verificar evento ForcedCloseByCutoff
            assertThat(eventCollector.getEvents().stream()
                    .anyMatch(e -> e.eventType().equals("ForcedCloseByCutoff"))).isTrue();
        } finally {
            com.pezbackend.shared.infrastructure.TenantContext.clear();
        }
    }

    @Test
    public void testConcurrentSaleTicketNumberGeneration() throws Exception {
        // Setup two tables and two orders in ALL_DELIVERED
        RestaurantTable table1 = restaurantTableRepository.save(new RestaurantTable(20, 1, "Zona A", 0, 0));
        Order order1 = orderCommandService.createOrder(table1.getId(), "DINE_IN", null);
        orderCommandService.addItemsToOrder(order1.getId(), productCeviche.getId(), 1, "Nota 1", 1L, "admin_user");
        Order updatedOrder1 = orderQueryService.getOrderById(order1.getId());
        OrderItem item1 = updatedOrder1.getItems().get(0);
        mockMvc.perform(post("/api/v1/orders/" + order1.getId() + "/items/" + item1.getId() + "/status")
                        .param("status", "DELIVERED")
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        RestaurantTable table2 = restaurantTableRepository.save(new RestaurantTable(21, 1, "Zona A", 0, 0));
        Order order2 = orderCommandService.createOrder(table2.getId(), "DINE_IN", null);
        orderCommandService.addItemsToOrder(order2.getId(), productCeviche.getId(), 1, "Nota 2", 1L, "admin_user");
        Order updatedOrder2 = orderQueryService.getOrderById(order2.getId());
        OrderItem item2 = updatedOrder2.getItems().get(0);
        mockMvc.perform(post("/api/v1/orders/" + order2.getId() + "/items/" + item2.getId() + "/status")
                        .param("status", "DELIVERED")
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        // We will execute the sale creation command concurrently using threads
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(2);

        java.util.concurrent.Future<String> future1 = executor.submit(() -> {
            try {
                barrier.await();
                String createSaleJson = String.format(
                        "{\"orderId\": %d, \"documentType\": \"BOLETA\", \"customerDocumentNumber\": \"10000001\"}",
                        order1.getId()
                );
                return mockMvc.perform(post("/api/v1/sales")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createSaleJson)
                                .with(user(adminDetails)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        java.util.concurrent.Future<String> future2 = executor.submit(() -> {
            try {
                barrier.await();
                String createSaleJson = String.format(
                        "{\"orderId\": %d, \"documentType\": \"BOLETA\", \"customerDocumentNumber\": \"10000002\"}",
                        order2.getId()
                );
                return mockMvc.perform(post("/api/v1/sales")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createSaleJson)
                                .with(user(adminDetails)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        String saleId1 = future1.get();
        String saleId2 = future2.get();

        executor.shutdown();

        Sale s1 = saleRepository.findById(Long.parseLong(saleId1)).orElseThrow();
        Sale s2 = saleRepository.findById(Long.parseLong(saleId2)).orElseThrow();

        assertThat(s1.getTicketNumber()).isNotNull();
        assertThat(s2.getTicketNumber()).isNotNull();
        assertThat(s1.getTicketNumber()).isNotEqualTo(s2.getTicketNumber());

        // Verify correlatives are sequential
        String num1 = s1.getTicketNumber().split("-")[1];
        String num2 = s2.getTicketNumber().split("-")[1];
        int val1 = Integer.parseInt(num1);
        int val2 = Integer.parseInt(num2);
        
        assertThat(Math.abs(val1 - val2)).isEqualTo(1);
    }
}
