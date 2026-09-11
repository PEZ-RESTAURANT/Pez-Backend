package com.pezbackend.tenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.tenancy.application.internal.commandservices.RestaurantCommandServiceImpl;
import com.pezbackend.tenancy.domain.model.aggregates.Restaurant;
import com.pezbackend.tenancy.domain.model.commands.OnboardingCommand;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.RestaurantRepository;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;
import com.pezbackend.tenancy.interfaces.rest.RestaurantController;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.PaymentMethodConfigRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.staff.domain.model.valueobjects.AttendanceMethod;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.StaffProfileRepository;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.AttendanceRecordRepository;
import com.pezbackend.staff.infrastructure.eventlisteners.StaffEventListener;
import com.pezbackend.cashregister.domain.model.events.ForcedCloseByCutoff;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TenantIsolationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantController restaurantController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService tokenService;

    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private StaffEventListener staffEventListener;

    @BeforeEach
    public void setUp() {
        // Asegurar que el rol ADMIN existe en el ambiente de test
        if (roleRepository.findByName(Roles.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Roles.ADMIN));
        }
    }

    @Test
    public void testOnboardingFlow() throws Exception {
        // Onboarding sin código de invitación -> 201 Created
        OnboardingResource validResource = new OnboardingResource("Restaurante A", "123456789", "contacto@restaurantea.com", "555-1234",
                "admin@restaurantea.com", "securePassword123", "Juan", "Perez", "TEST-INVITE-CODE");

        mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Restaurante A"));
    }

    @Test
    public void testTenantIsolationBetweenInquilinos() throws Exception {
        // 1. Crear Tenant A mediante onboarding directo
        OnboardingResource resourceA = new OnboardingResource("Tenant A", "20123456789", "info@tenanta.com", "999111222",
                "admin@tenanta.com", "passA", "Admin", "A", "TEST-INVITE-CODE");
        String responseA = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long idA = objectMapper.readTree(responseA).get("id").asLong();

        // 2. Crear Tenant B mediante onboarding directo
        OnboardingResource resourceB = new OnboardingResource("Tenant B", "20987654321", "info@tenantb.com", "999333444",
                "admin@tenantb.com", "passB", "Admin", "B", "TEST-INVITE-CODE");
        String responseB = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long idB = objectMapper.readTree(responseB).get("id").asLong();

        // Obtener los usuarios admin de la base de datos para simular llamadas HTTP autenticadas
        User adminA = userRepository.findByEmail("admin@tenanta.com")
                .orElseThrow(() -> new AssertionError("Admin A not found"));
        User adminB = userRepository.findByEmail("admin@tenantb.com")
                .orElseThrow(() -> new AssertionError("Admin B not found"));

        // Generar tokens reales que contengan los claims restaurantId correspondientes
        String tokenA = tokenService.generateToken(adminA.getId(), Roles.ADMIN.name(), idA);
        String tokenB = tokenService.generateToken(adminB.getId(), Roles.ADMIN.name(), idB);

        // 3. Admin A consulta Tenant A -> 200 OK
        mockMvc.perform(get("/api/v1/restaurants/" + idA)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idA))
                .andExpect(jsonPath("$.name").value("Tenant A"));

        // 4. Admin A consulta Tenant B -> 404 Not Found (aislamiento)
        mockMvc.perform(get("/api/v1/restaurants/" + idB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // 5. Admin B consulta Tenant B -> 200 OK
        mockMvc.perform(get("/api/v1/restaurants/" + idB)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idB))
                .andExpect(jsonPath("$.name").value("Tenant B"));
    }

    @Test
    public void shouldSetTenantContextBeforeSavingUserDuringOnboarding() {
        // Arrange (usando mocks para verificar la secuencia temporal y el estado exacto de TenantContext)
        RestaurantRepository mockRestaurantRepository = mock(RestaurantRepository.class);
        UserRepository mockUserRepository = mock(UserRepository.class);
        RoleRepository mockRoleRepository = mock(RoleRepository.class);
        HashingService mockHashingService = mock(HashingService.class);
        PaymentMethodConfigRepository mockPaymentMethodConfigRepository = mock(PaymentMethodConfigRepository.class);
        CategoryRepository mockCategoryRepository = mock(CategoryRepository.class);
        com.pezbackend.billing.infrastructure.persistence.jpa.repositories.BillingSequenceRepository mockBillingSequenceRepository = mock(com.pezbackend.billing.infrastructure.persistence.jpa.repositories.BillingSequenceRepository.class);

        RestaurantCommandServiceImpl service = new RestaurantCommandServiceImpl(
                mockRestaurantRepository, mockUserRepository, mockRoleRepository, mockHashingService, mockPaymentMethodConfigRepository, mockCategoryRepository, mockBillingSequenceRepository
        );


        OnboardingCommand command = new OnboardingCommand("Mock Rest", "111", "mock@mock.com", "000",
                "admin@mock.com", "pass", "A", "B", "TEST-INVITE-CODE");

        Restaurant savedRestaurant = new Restaurant("Mock Rest", "111", "mock@mock.com", "000");
        ReflectionTestUtils.setField(savedRestaurant, "id", 100L); // Asignar ID simulado 100

        when(mockRestaurantRepository.save(any(Restaurant.class))).thenReturn(savedRestaurant);
        when(mockUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(new Role(Roles.ADMIN)));
        when(mockHashingService.encode(anyString())).thenReturn("hashedpass");

        // Simular save del User verificando que TenantContext esté activo y contenga el ID correcto
        when(mockUserRepository.save(any(User.class))).thenAnswer(invocation -> {
            assertEquals(100L, TenantContext.getCurrentTenantId(),
                    "TenantContext debe estar seteado con el ID de restaurante generado antes de guardar al usuario/auditoría");
            return invocation.getArgument(0);
        });

        // Act
        Restaurant result = service.handleOnboarding(command);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(mockUserRepository, times(1)).save(any(User.class));

        // Validar que el contexto se limpie correctamente al terminar el método
        assertNull(TenantContext.getCurrentTenantId(),
                "TenantContext debe limpiarse tras finalizar el flujo de onboarding");
    }



    @Test
    public void testOnboardingRateLimiting() throws Exception {
        // Reset rate limits and activeProfile to simulate production behavior
        restaurantController.resetRateLimits();
        org.springframework.test.util.ReflectionTestUtils.setField(restaurantController, "activeProfile", "prod");

        try {
            // Perform 5 requests (all should pass successfully)
            for (int i = 0; i < 5; i++) {
                OnboardingResource uniqueResource = new OnboardingResource(
                        "Restaurante Rate " + i, "99999999" + i, "rate" + i + "@rest.com", "555-5555",
                        "admin" + i + "@rate.com", "pass", "A", "B", "TEST-INVITE-CODE"
                );
                mockMvc.perform(post("/api/v1/restaurants/onboarding")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(uniqueResource)))
                        .andExpect(status().isCreated());
            }

            // 6th request should trigger rate limit (429)
            OnboardingResource limitResource = new OnboardingResource(
                    "Restaurante Limit", "888888888", "limit@rest.com", "555-5555",
                    "admin_limit@rate.com", "pass", "A", "B", "TEST-INVITE-CODE"
            );

            mockMvc.perform(post("/api/v1/restaurants/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(limitResource)))
                    .andExpect(status().is4xxClientError()); // TOO_MANY_REQUESTS (429)
        } finally {
            // Restore activeProfile to test to avoid affecting other tests
            org.springframework.test.util.ReflectionTestUtils.setField(restaurantController, "activeProfile", "test");
        }
    }

    @Test
    public void testTenantIsolationForUnresolvedAttendanceAlerts() throws Exception {
        // 1. Crear Tenant A mediante onboarding directo
        OnboardingResource resourceA = new OnboardingResource("Tenant A", "30123456789", "infoA@tenanta.com", "999111222",
                "adminA@tenanta.com", "passA", "Admin", "A", "TEST-INVITE-CODE");
        String responseA = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long idA = objectMapper.readTree(responseA).get("id").asLong();

        // 2. Crear Tenant B mediante onboarding directo
        OnboardingResource resourceB = new OnboardingResource("Tenant B", "30987654321", "infoB@tenantb.com", "999333444",
                "adminB@tenantb.com", "passB", "Admin", "B", "TEST-INVITE-CODE");
        String responseB = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long idB = objectMapper.readTree(responseB).get("id").asLong();

        User adminA = userRepository.findByEmail("adminA@tenanta.com")
                .orElseThrow(() -> new AssertionError("Admin A not found"));
        User adminB = userRepository.findByEmail("adminB@tenantb.com")
                .orElseThrow(() -> new AssertionError("Admin B not found"));

        // 3. Crear perfiles de personal y registros de asistencia sin salida para Tenant A y Tenant B
        TenantContext.setCurrentTenantId(idA);
        StaffProfile profileA = new StaffProfile(adminA.getId(), StaffPaymentType.HOURLY, new BigDecimal("15.00"));
        profileA = staffProfileRepository.save(profileA);
        AttendanceRecord attendanceA = new AttendanceRecord(profileA.getId(), LocalDateTime.now().minusHours(2), AttendanceMethod.MANUAL_BY_ADMIN);
        attendanceA = attendanceRecordRepository.save(attendanceA);
        TenantContext.clear();

        TenantContext.setCurrentTenantId(idB);
        StaffProfile profileB = new StaffProfile(adminB.getId(), StaffPaymentType.HOURLY, new BigDecimal("20.00"));
        profileB = staffProfileRepository.save(profileB);
        AttendanceRecord attendanceB = new AttendanceRecord(profileB.getId(), LocalDateTime.now().minusHours(3), AttendanceMethod.MANUAL_BY_ADMIN);
        attendanceB = attendanceRecordRepository.save(attendanceB);
        TenantContext.clear();

        // 4. Invocar el StaffEventListener para Tenant A simulando el cierre de caja de Tenant A
        staffEventListener.onForcedCloseByCutoff(new ForcedCloseByCutoff(1L, LocalDateTime.now(), idA));

        // 5. Verificar que el registro de Tenant A se marcó como no resuelto (unresolved = true)
        TenantContext.setCurrentTenantId(idA);
        Optional<AttendanceRecord> updatedA = attendanceRecordRepository.findById(attendanceA.getId());
        assertTrue(updatedA.isPresent());
        assertTrue(updatedA.get().isUnresolved(), "La asistencia del Tenant A debe marcarse como no resuelta al cerrar caja en Tenant A");
        TenantContext.clear();

        // 6. Verificar que el registro de Tenant B NO se alteró (unresolved = false)
        TenantContext.setCurrentTenantId(idB);
        Optional<AttendanceRecord> updatedB = attendanceRecordRepository.findById(attendanceB.getId());
        assertTrue(updatedB.isPresent());
        assertFalse(updatedB.get().isUnresolved(), "La asistencia del Tenant B NO debe verse afectada por el cierre de caja en Tenant A");
        TenantContext.clear();
    }
}
