package com.pezbackend.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.interfaces.rest.resources.CategoryResource;
import com.pezbackend.catalog.interfaces.rest.resources.CreateProductResource;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;
import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.KitchenZoneRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.orders.interfaces.rest.resources.CreateTableResource;
import com.pezbackend.kitchen.interfaces.rest.resources.CreateKitchenZoneResource;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CatalogIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private KitchenZoneRepository kitchenZoneRepository;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository restaurantTableRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    public void setUp() {
        if (roleRepository.findByName(Roles.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Roles.ADMIN));
        }
    }

    @Test
    public void testOnboardingCreatesSeededCategories() throws Exception {
        // 1. Registrar un nuevo restaurante A
        OnboardingResource onboardingResource = new OnboardingResource(
                "Restaurante Test Seeding", "20777777777", "contacto@testseeding.com", "555-777",
                "admin@testseeding.com", "securePass123", "Carlos", "Soto"
        );

        String response = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboardingResource)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long tenantId = objectMapper.readTree(response).get("id").asLong();

        // 2. Obtener token del admin creado
        User admin = userRepository.findByEmail("admin@testseeding.com")
                .orElseThrow(() -> new AssertionError("Admin not found"));
        String token = tokenService.generateToken(admin.getId(), Roles.ADMIN.name(), tenantId);

        // 3. Consultar las categorías y verificar que no se sembró ninguna por defecto (catálogo vacío)
        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testCannotDeleteCategoryWithProducts() throws Exception {
        // 1. Registrar un restaurante para tener un tenant context limpio
        OnboardingResource onboardingResource = new OnboardingResource(
                "Restaurante Test Delete", "20888888888", "contacto@testdel.com", "555-888",
                "admin@testdel.com", "securePass123", "Pedro", "Gomez"
        );

        String response = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboardingResource)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long tenantId = objectMapper.readTree(response).get("id").asLong();

        User admin = userRepository.findByEmail("admin@testdel.com")
                .orElseThrow(() -> new AssertionError("Admin not found"));
        String token = tokenService.generateToken(admin.getId(), Roles.ADMIN.name(), tenantId);

        // Crear una categoría adicional manualmente
        String categoryJson = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryResource(null, "Especiales"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long categoryId = objectMapper.readTree(categoryJson).get("id").asLong();

        // Crear un producto asociado a esa categoría
        CreateProductResource productResource = new CreateProductResource("Ceviche Royal", new BigDecimal("55.00"), categoryId);
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productResource)))
                .andExpect(status().isOk());

        // Intentar eliminar la categoría -> debe arrojar 400 Bad Request
        mockMvc.perform(delete("/api/v1/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede eliminar una categoría con productos activos."));
    }

    @Test
    public void testTenantIsolationBetweenCategories() throws Exception {
        // 1. Crear Restaurante A (Tenant A)
        OnboardingResource resourceA = new OnboardingResource(
                "Restaurante A Isolation", "20111111111", "contacto@resta.com", "555-111",
                "admin@resta.com", "securePassA", "Ana", "Ruiz"
        );
        String responseA = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long tenantIdA = objectMapper.readTree(responseA).get("id").asLong();
        User adminA = userRepository.findByEmail("admin@resta.com").orElseThrow();
        String tokenA = tokenService.generateToken(adminA.getId(), Roles.ADMIN.name(), tenantIdA);

        // 2. Crear Restaurante B (Tenant B)
        OnboardingResource resourceB = new OnboardingResource(
                "Restaurante B Isolation", "20222222222", "contacto@restb.com", "555-222",
                "admin@restb.com", "securePassB", "Beto", "Diaz"
        );
        String responseB = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long tenantIdB = objectMapper.readTree(responseB).get("id").asLong();
        User adminB = userRepository.findByEmail("admin@restb.com").orElseThrow();
        String tokenB = tokenService.generateToken(adminB.getId(), Roles.ADMIN.name(), tenantIdB);

        // 3. Crear Categoría "Marina A" en el Tenant A
        String catJsonA = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryResource(null, "Marina A"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long catIdA = objectMapper.readTree(catJsonA).get("id").asLong();

        // 4. Crear Categoría "Criolla B" en el Tenant B
        String catJsonB = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryResource(null, "Criolla B"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long catIdB = objectMapper.readTree(catJsonB).get("id").asLong();

        // 5. Tenant A consulta categorías -> No debe ver "Criolla B" pero sí "Marina A"
        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // 1 agregada
                .andExpect(jsonPath("$[?(@.name == 'Marina A')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Criolla B')]").doesNotExist());

        // 6. Tenant B consulta categorías -> No debe ver "Marina A" pero sí "Criolla B"
        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // 1 agregada
                .andExpect(jsonPath("$[?(@.name == 'Criolla B')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Marina A')]").doesNotExist());

        // 7. Tenant A intenta borrar "Criolla B" -> Debe fallar con 404 Not Found (aislamiento)
        mockMvc.perform(delete("/api/v1/categories/" + catIdB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testProductKitchenZoneAssignment() throws Exception {
        // 1. Setup Tenant/User
        OnboardingResource onboardingResource = new OnboardingResource(
                "Restaurante Test Kitchen", "20999999999", "contacto@testkitchen.com", "555-999",
                "admin@testkitchen.com", "securePass123", "Carlos", "Soto"
        );

        String response = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboardingResource)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long tenantId = objectMapper.readTree(response).get("id").asLong();

        User admin = userRepository.findByEmail("admin@testkitchen.com")
                .orElseThrow(() -> new AssertionError("Admin not found"));
        String token = tokenService.generateToken(admin.getId(), Roles.ADMIN.name(), tenantId);

        // 2. Crear Categoría y Producto
        TenantContext.setCurrentTenantId(tenantId);
        Category cat = new Category("Pescados");
        cat.setRestaurantId(tenantId);
        cat = categoryRepository.save(cat);

        Product prod = new Product("Ceviche Clásico", new java.math.BigDecimal("35.00"), cat, 10, true);
        prod.setRestaurantId(tenantId);
        prod = productRepository.save(prod);

        // 3. Crear una zona de cocina
        KitchenZone zone = new KitchenZone("Fríos");
        zone.setRestaurantId(tenantId);
        zone = kitchenZoneRepository.save(zone);

        // 4. Asignar producto a zona
        mockMvc.perform(put("/api/v1/products/" + prod.getId() + "/kitchen-zone")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zoneId\": " + zone.getId() + "}"))
                .andExpect(status().isOk());

        // 5. Consultar asignación
        mockMvc.perform(get("/api/v1/products/" + prod.getId() + "/kitchen-zone")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneId").value(zone.getId()));

        // 6. Remover de zona (unassign) enviando null
        mockMvc.perform(put("/api/v1/products/" + prod.getId() + "/kitchen-zone")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zoneId\": null}"))
                .andExpect(status().isOk());

        // 7. Verificar que no está asignado y devuelve null
        mockMvc.perform(get("/api/v1/products/" + prod.getId() + "/kitchen-zone")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneId").value(org.hamcrest.Matchers.nullValue()));

        // 8. Reasignar a zona y luego eliminar la zona de cocina
        mockMvc.perform(put("/api/v1/products/" + prod.getId() + "/kitchen-zone")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zoneId\": " + zone.getId() + "}"))
                .andExpect(status().isOk());

        // Eliminar zona de cocina
        mockMvc.perform(delete("/api/v1/kitchen/zones/" + zone.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verificar que el producto sigue existiendo y su zona es null
        mockMvc.perform(get("/api/v1/products/" + prod.getId() + "/kitchen-zone")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneId").value(org.hamcrest.Matchers.nullValue()));

        mockMvc.perform(get("/api/v1/products/" + prod.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ceviche Clásico"));
    }

    @Test
    public void testTenantIsolationOnCoreEntities() throws Exception {
        // 1. Crear Restaurante Tenant A
        OnboardingResource onboardingA = new OnboardingResource(
                "Restaurante Tenant A", "20111111111", "contacto@tenanta.com", "555-111",
                "admin@tenanta.com", "secureA123", "Admin", "A"
        );
        String responseA = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboardingA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long tenantIdA = objectMapper.readTree(responseA).get("id").asLong();

        User adminA = userRepository.findByEmail("admin@tenanta.com")
                .orElseThrow(() -> new AssertionError("Admin A not found"));
        String tokenA = tokenService.generateToken(adminA.getId(), Roles.ADMIN.name(), tenantIdA);

        // 2. Crear Restaurante Tenant B
        OnboardingResource onboardingB = new OnboardingResource(
                "Restaurante Tenant B", "20222222222", "contacto@tenantb.com", "555-222",
                "admin@tenantb.com", "secureB123", "Admin", "B"
        );
        String responseB = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboardingB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long tenantIdB = objectMapper.readTree(responseB).get("id").asLong();

        User adminB = userRepository.findByEmail("admin@tenantb.com")
                .orElseThrow(() -> new AssertionError("Admin B not found"));
        String tokenB = tokenService.generateToken(adminB.getId(), Roles.ADMIN.name(), tenantIdB);

        // ==========================================
        // 3. AISLAMIENTO DE CATEGORÍAS (Category)
        // ==========================================
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Entradas A\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Platos de Fondo B\"}"))
                .andExpect(status().isCreated());

        // Consultar desde A
        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Entradas A"));

        // Consultar desde B
        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Platos de Fondo B"));

        // ==========================================
        // 4. AISLAMIENTO DE MESAS (RestaurantTable)
        // ==========================================
        CreateTableResource tableA = new CreateTableResource(10, 1, "Salón A", 10, 10);
        mockMvc.perform(post("/api/v1/tables")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tableA)))
                .andExpect(status().isCreated());

        CreateTableResource tableB = new CreateTableResource(20, 1, "Salón B", 20, 20);
        mockMvc.perform(post("/api/v1/tables")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tableB)))
                .andExpect(status().isCreated());

        // Consultar desde A
        mockMvc.perform(get("/api/v1/tables")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value(10));

        // Consultar desde B
        mockMvc.perform(get("/api/v1/tables")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value(20));

        // ==========================================
        // 5. AISLAMIENTO DE ZONAS DE COCINA (KitchenZone)
        // ==========================================
        CreateKitchenZoneResource zoneA = new CreateKitchenZoneResource("Cocina Principal A");
        mockMvc.perform(post("/api/v1/kitchen/zones")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneA)))
                .andExpect(status().isOk());

        CreateKitchenZoneResource zoneB = new CreateKitchenZoneResource("Bar B");
        mockMvc.perform(post("/api/v1/kitchen/zones")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneB)))
                .andExpect(status().isOk());

        // Consultar desde A
        mockMvc.perform(get("/api/v1/kitchen/zones")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cocina Principal A"));

        // Consultar desde B
        mockMvc.perform(get("/api/v1/kitchen/zones")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bar B"));
    }
}
