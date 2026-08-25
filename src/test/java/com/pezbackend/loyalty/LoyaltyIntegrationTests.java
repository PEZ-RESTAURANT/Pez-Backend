package com.pezbackend.loyalty;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.domain.model.entities.SatisfactionSurvey;
import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;
import com.pezbackend.loyalty.domain.model.valueobjects.PointsTransactionType;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoyaltyIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SatisfactionSurveyRepository satisfactionSurveyRepository;

    @Autowired
    private PointsTransactionRepository pointsTransactionRepository;

    @Autowired
    private LoyaltyConfigRepository loyaltyConfigRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private SaleCommandService saleCommandService;

    private User adminUser;
    private UserDetailsImpl adminDetails;
    private Product ceviche;

    @BeforeEach
    public void setUp() {
        // Limpiar base de datos
        satisfactionSurveyRepository.deleteAll();
        pointsTransactionRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        restaurantTableRepository.deleteAll();

        // Sembrar roles y usuarios de seguridad
        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));

        adminUser = new User("admin@pez.com", "pw", "Admin", "User", true);
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminDetails = UserDetailsImpl.build(adminUser);

        // Crear producto de prueba con categoría dinámica
        Category marina = categoryRepository.save(new Category("Marina"));
        ceviche = new Product("Ceviche Carretillero", new BigDecimal("35.00"), marina);
        ceviche = productRepository.save(ceviche);

        // Inicializar configuración de fidelización por defecto si no existe
        if (loyaltyConfigRepository.count() == 0) {
            loyaltyConfigRepository.save(new LoyaltyConfig(
                    new BigDecimal("10.00"),
                    new BigDecimal("1.00"),
                    4,
                    "https://google.com/review"
            ));
        }
    }

    @Test
    public void testCreateCustomerWithoutConsentThrowsException() throws Exception {
        String createJson = "{\"phone\": \"987654321\", \"fullName\": \"Juan Perez\", \"birthday\": \"1990-05-15\", \"address\": \"Av. Lima 123\", \"dataConsentAccepted\": false}";

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .with(user(adminDetails)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode", is("DATA_CONSENT_REQUIRED")));
    }

    @Test
    public void testSaleMarkedPaidGrantsPoints() throws Exception {
        // Establecer autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities())
        );

        // 1. Crear cliente con consentimiento
        Customer customer = new Customer("987654321", "Juan Perez", LocalDate.of(1990, 5, 15), "Av. Lima 123", true);
        customer = customerRepository.save(customer);

        // 2. Crear mesa física
        RestaurantTable table = orderCommandService.createTable(99, 1, "Central", 100, 100);

        // Crear la orden ligada al cliente directamente en la mesa FREE
        Order order = orderCommandService.createOrder(table.getId(), "DINE_IN", customer.getId());
        orderCommandService.addItemsToOrder(order.getId(), ceviche.getId(), 2, "Sin picante", 1L, "waiter_user");

        // Cargar orden con sus ítems guardados
        order = orderQueryService.getOrderById(order.getId());
        OrderItem item = order.getItems().get(0);

        // Transicionar plato a DELIVERED
        orderCommandService.changeItemStatus(order.getId(), item.getId(), "IN_PREPARATION", "cook_user");
        orderCommandService.changeItemStatus(order.getId(), item.getId(), "READY", "cook_user");
        orderCommandService.changeItemStatus(order.getId(), item.getId(), "DELIVERED", "waiter_user");

        // 4. Emitir comprobante (generar venta)
        CreateSaleCommand createSaleCmd = new CreateSaleCommand(order.getId(), DocumentType.BOLETA, "72846193", "Juan Perez");
        Long saleId = saleCommandService.handle(createSaleCmd);

        // 5. Registrar el pago completo (monto = 70.00)
        saleCommandService.registerPayments(
                saleId,
                List.of(new PaymentDetail(PaymentMethod.CASH, new BigDecimal("70.00"))),
                "admin@pez.com"
        );

        // 6. Verificar que el balance de puntos se actualizó a 70 puntos
        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(updatedCustomer.getPointsBalance()).isEqualTo(70);

        // Verificar transacción de acumulación en base de datos
        List<PointsTransaction> transactions = pointsTransactionRepository.findAllByCustomerId(customer.getId());
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getType()).isEqualTo(PointsTransactionType.EARNED);
        assertThat(transactions.get(0).getAmount()).isEqualTo(70);
    }

    @Test
    public void testRedeemPointsWithInsufficientBalanceFails() throws Exception {
        // 1. Crear cliente con consentimiento y balance inicial = 10 puntos
        Customer customer = new Customer("911222333", "Maria Gomez", LocalDate.of(1995, 8, 20), "Calle 5", true);
        customer.earnPoints(10);
        customer = customerRepository.save(customer);

        // 2. Intentar canjear 50 puntos (saldo insuficiente)
        String redeemJson = "{\"points\": 50, \"date\": \"2026-08-01\"}";
        mockMvc.perform(post("/api/v1/customers/" + customer.getId() + "/redeem-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redeemJson)
                        .with(user(adminDetails)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode", is("INSUFFICIENT_POINTS")));
    }

    @Test
    public void testDeleteCustomerCascadesDeletion() throws Exception {
        // 1. Crear cliente
        Customer customer = new Customer("933444555", "Carlos Soto", LocalDate.of(1985, 12, 10), "Jr. Junin 456", true);
        customer.earnPoints(100);
        customer = customerRepository.save(customer);
        Long customerId = customer.getId();

        // 2. Agregar una encuesta
        SatisfactionSurvey survey = new SatisfactionSurvey(
                customerId, "Ceviche", "Chicha", 5, 5, "Excelente servicio", LocalDate.now()
        );
        satisfactionSurveyRepository.save(survey);

        // 3. Agregar una transacción
        PointsTransaction txn = new PointsTransaction(customerId, PointsTransactionType.REDEEMED, 20, null, LocalDate.now());
        pointsTransactionRepository.save(txn);

        // 4. Validar existencia
        assertThat(satisfactionSurveyRepository.findAllByCustomerId(customerId)).hasSize(1);
        assertThat(pointsTransactionRepository.findAllByCustomerId(customerId)).hasSize(1);

        // 5. Borrar cliente mediante DELETE endpoint
        mockMvc.perform(delete("/api/v1/customers/" + customerId)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // 6. Validar eliminación física en cascada programada
        assertThat(customerRepository.findById(customerId).isPresent()).isFalse();
        assertThat(satisfactionSurveyRepository.findAllByCustomerId(customerId)).isEmpty();
        assertThat(pointsTransactionRepository.findAllByCustomerId(customerId)).isEmpty();
    }
}
