package com.pezbackend.analytics;

import com.pezbackend.analytics.domain.model.entities.AnalyticsConfig;
import com.pezbackend.analytics.infrastructure.persistence.jpa.repositories.AnalyticsConfigRepository;
import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import com.pezbackend.catalog.domain.model.entities.ProductKitchenZone;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductKitchenZoneRepository;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.KitchenZoneRepository;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AnalyticsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductKitchenZoneRepository productKitchenZoneRepository;

    @Autowired
    private KitchenZoneRepository kitchenZoneRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleCommandService saleCommandService;

    @Autowired
    private CashRegisterRepository cashRegisterRepository;

    @Autowired
    private AnalyticsConfigRepository analyticsConfigRepository;

    private UserDetailsImpl adminDetails;
    private Product ceviche;
    private KitchenZone zoneMarina;
    private User adminUser;

    @BeforeEach
    public void setUp() {
        // Limpiar base de datos
        analyticsConfigRepository.deleteAll();
        saleRepository.deleteAll();
        orderRepository.deleteAll();
        restaurantTableRepository.deleteAll();
        cashRegisterRepository.deleteAll();
        productKitchenZoneRepository.deleteAll();
        kitchenZoneRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // 1. Crear roles y usuario admin
        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));
        adminUser = new User("admin@pez.com", "password123", "Carlos", "Perez", true);
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminDetails = UserDetailsImpl.build(adminUser);

        // 2. Crear plato y zona
        ceviche = productRepository.save(new Product("Ceviche Clásico", BigDecimal.valueOf(35.00), ProductCategory.MARINA));
        zoneMarina = kitchenZoneRepository.save(new KitchenZone("MARINA"));
        productKitchenZoneRepository.save(new ProductKitchenZone(ceviche.getId(), zoneMarina.getId()));

        // Establecer contexto de seguridad para la ejecución programática
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities())
        );

        // Seed default config
        analyticsConfigRepository.save(new AnalyticsConfig(5, 30, "{}"));
    }

    @Test
    @Transactional
    public void testAnalyticsEndToEndFlow() throws Exception {
        // 1. Configurar caja con un egreso manual
        CashRegister register = new CashRegister(BigDecimal.valueOf(200.00));
        // Agregar egreso (gasto de 15.00 soles)
        CashMovement expense = new CashMovement(CashMovementType.EXPENSE, BigDecimal.valueOf(15.00), "Compra de Limones");
        register.addMovement(expense);
        cashRegisterRepository.save(register);

        // 2. Crear mesa y comanda
        RestaurantTable table = restaurantTableRepository.save(new RestaurantTable(15, 4, "Salon Principal", 100, 100));
        Order order = orderCommandService.createOrder(table.getId(), "DINE_IN", null);
        orderCommandService.addItemsToOrder(order.getId(), ceviche.getId(), 2, "Con ají limo", 1L, adminUser.getEmail());

        order = orderQueryService.getOrderById(order.getId());
        OrderItem item = order.getItems().get(0);

        // Transicionar ítem para que cuente en cocina y producción diaria
        orderCommandService.changeItemStatus(order.getId(), item.getId(), "IN_PREPARATION", adminUser.getEmail());
        orderCommandService.changeItemStatus(order.getId(), item.getId(), "READY", adminUser.getEmail());
        orderCommandService.changeItemStatus(order.getId(), item.getId(), "DELIVERED", adminUser.getEmail());

        // 3. Crear venta y marcar como pagada
        CreateSaleCommand createSaleCmd = new CreateSaleCommand(order.getId(), DocumentType.BOLETA, "74859123", "Cliente Anonimo");
        Long saleId = saleCommandService.handle(createSaleCmd);

        PaymentDetail cashPayment = new PaymentDetail(PaymentMethod.CASH, BigDecimal.valueOf(70.00));
        saleCommandService.registerPayments(saleId, List.of(cashPayment), adminUser.getEmail());

        // 4. Validar reportes vía REST API

        // Daily Revenue: debe ser 70.00
        mockMvc.perform(get("/api/v1/analytics/revenue/daily")
                        .param("date", LocalDate.now().toString())
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue", is(70.0)));

        // Net Profit: Revenue 70.0 - Expense 15.0 = Net Profit 55.0
        mockMvc.perform(get("/api/v1/analytics/net-profit")
                        .param("from", LocalDate.now().toString())
                        .param("to", LocalDate.now().toString())
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue", is(70.0)))
                .andExpect(jsonPath("$.totalExpenses", is(15.0)))
                .andExpect(jsonPath("$.netProfit", is(55.0)));

        // Waiter ranking: adminUser (waiterId = adminUser.id) vendió 70.0
        mockMvc.perform(get("/api/v1/analytics/waiters/ranking")
                        .param("from", LocalDate.now().toString())
                        .param("to", LocalDate.now().toString())
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].waiterId", is(adminUser.getId().intValue())))
                .andExpect(jsonPath("$[0].totalSales", is(70.0)));

        // Export PDF
        mockMvc.perform(get("/api/v1/analytics/export")
                        .param("report", "daily-revenue")
                        .param("format", "pdf")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // Export Excel
        mockMvc.perform(get("/api/v1/analytics/export")
                        .param("report", "waiters-ranking")
                        .param("format", "excel")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
    }
}
