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
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.PaymentMethodConfigRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
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

import java.util.Optional;

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

    @BeforeEach
    public void setUp() {
        // Asegurar que el rol ADMIN existe en el ambiente de test
        if (roleRepository.findByName(Roles.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Roles.ADMIN));
        }
    }

    @Test
    public void testOnboardingInviteCodeValidation() throws Exception {
        // 1. Onboarding con código de invitación incorrecto -> 403 Forbidden
        OnboardingResource invalidResource = new OnboardingResource(
                "Restaurante Falso", "123456", "test@test.com", "9999",
                "admin@falso.com", "pass123", "Admin", "User", "INVALID-CODE"
        );

        mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidResource)))
                .andExpect(status().isForbidden());

        // 2. Onboarding con código de invitación correcto (TEST-INVITE-CODE configurado en application-test.properties) -> 201 Created
        OnboardingResource validResource = new OnboardingResource(
                "Restaurante A", "123456789", "contacto@restaurantea.com", "555-1234",
                "admin@restaurantea.com", "securePassword123", "Juan", "Perez", "TEST-INVITE-CODE"
        );

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
        OnboardingResource resourceA = new OnboardingResource(
                "Tenant A", "20123456789", "info@tenanta.com", "999111222",
                "admin@tenanta.com", "passA", "Admin", "A", "TEST-INVITE-CODE"
        );
        String responseA = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long idA = objectMapper.readTree(responseA).get("id").asLong();

        // 2. Crear Tenant B mediante onboarding directo
        OnboardingResource resourceB = new OnboardingResource(
                "Tenant B", "20987654321", "info@tenantb.com", "999333444",
                "admin@tenantb.com", "passB", "Admin", "B", "TEST-INVITE-CODE"
        );
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

        RestaurantCommandServiceImpl service = new RestaurantCommandServiceImpl(
                mockRestaurantRepository, mockUserRepository, mockRoleRepository, mockHashingService, mockPaymentMethodConfigRepository, mockCategoryRepository
        );

        ReflectionTestUtils.setField(service, "expectedInviteCode", "MOCK-INVITE");

        OnboardingCommand command = new OnboardingCommand(
                "Mock Rest", "111", "mock@mock.com", "000",
                "admin@mock.com", "pass", "A", "B", "MOCK-INVITE"
        );

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
}
