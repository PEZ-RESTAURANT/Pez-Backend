package com.pezbackend.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.model.valueobjects.ReservationStatus;
import com.pezbackend.orders.domain.model.valueobjects.TableStatus;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository;
import com.pezbackend.orders.interfaces.rest.resources.CreateTableResource;
import com.pezbackend.orders.interfaces.rest.resources.CreateReservationResource;
import com.pezbackend.orders.interfaces.rest.resources.MergeTablesResource;
import com.pezbackend.orders.interfaces.rest.resources.TransferOrderResource;
import com.pezbackend.orders.interfaces.rest.resources.UpdateReservationResource;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class OrdersExtensionsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private com.pezbackend.orders.domain.services.OrderCommandService orderCommandService;

    @Autowired
    private com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository orderRepository;

    @Autowired
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private String tokenA;
    private Long idA;

    private String tokenB;
    private Long idB;

    @BeforeEach
    public void setUp() throws Exception {
        // Asegurar que el rol ADMIN existe
        if (roleRepository.findByName(Roles.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Roles.ADMIN));
        }

        // Onboarding Tenant A
        OnboardingResource resourceA = new OnboardingResource("Tenant A", "20123456789", "info@tenanta.com", "999111222",
                "admin@tenanta.com", "passA", "Admin", "A", "TEST-INVITE-CODE");
        String responseA = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        idA = objectMapper.readTree(responseA).get("id").asLong();

        // Onboarding Tenant B
        OnboardingResource resourceB = new OnboardingResource("Tenant B", "20987654321", "info@tenantb.com", "999333444",
                "admin@tenantb.com", "passB", "Admin", "B", "TEST-INVITE-CODE");
        String responseB = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        idB = objectMapper.readTree(responseB).get("id").asLong();

        User adminA = userRepository.findByEmail("admin@tenanta.com").orElseThrow();
        User adminB = userRepository.findByEmail("admin@tenantb.com").orElseThrow();

        tokenA = tokenService.generateToken(adminA.getId(), Roles.ADMIN.name(), idA);
        tokenB = tokenService.generateToken(adminB.getId(), Roles.ADMIN.name(), idB);
    }

    private Long createTable(int number, String token) throws Exception {
        CreateTableResource resource = new CreateTableResource(number, 1, "Salón Principal", 10, 20);
        String response = mockMvc.perform(post("/api/v1/tables")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    public void testTableMergingAndUnmergingFlow() throws Exception {
        // 1. Crear mesas en Tenant A
        Long t1 = createTable(100, tokenA);
        Long t2 = createTable(101, tokenA);
        Long t3 = createTable(102, tokenA);

        // 2. Fusionar mesas t2 y t3 bajo el ancla t1
        MergeTablesResource mergeResource = new MergeTablesResource(List.of(t2, t3));
        mockMvc.perform(post("/api/v1/tables/" + t1 + "/merge")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mergeResource)))
                .andExpect(status().isNoContent());

        // Verificar el estado en base de datos
        RestaurantTable table2 = restaurantTableRepository.findById(t2).orElseThrow();
        assertEquals(t1, table2.getAnchorTableId());

        RestaurantTable table3 = restaurantTableRepository.findById(t3).orElseThrow();
        assertEquals(t1, table3.getAnchorTableId());

        // 3. Verificar que no se puede abrir un pedido o solicitar atención en una mesa fusionada (no ancla)
        mockMvc.perform(post("/api/v1/tables/" + t2 + "/request-attention")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict()); // BusinessRuleViolationException ("TABLE_MERGED")

        // 4. Solicitar atención en el ancla -> cambia estado de todas las mesas a UNATTENDED
        mockMvc.perform(post("/api/v1/tables/" + t1 + "/request-attention")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        assertEquals(TableStatus.UNATTENDED, restaurantTableRepository.findById(t1).orElseThrow().getStatus());
        assertEquals(TableStatus.UNATTENDED, restaurantTableRepository.findById(t2).orElseThrow().getStatus());
        assertEquals(TableStatus.UNATTENDED, restaurantTableRepository.findById(t3).orElseThrow().getStatus());

        // 5. Atender mesa ancla -> TAKING_ORDER
        mockMvc.perform(post("/api/v1/tables/" + t1 + "/attend")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        assertEquals(TableStatus.TAKING_ORDER, restaurantTableRepository.findById(t1).orElseThrow().getStatus());
        assertEquals(TableStatus.TAKING_ORDER, restaurantTableRepository.findById(t2).orElseThrow().getStatus());

        // 6. Verificar que no se puede deshacer la fusión manualmente si hay un pedido en curso
        mockMvc.perform(post("/api/v1/tables/" + t1 + "/unmerge")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict());

        // 7. Simular el cierre/pago del pedido para liberar la mesa ancla -> todas vuelven a FREE y se limpia el anchorTableId
        // Para simplificar, buscamos la comanda activa y la pagamos
        String ordersResponse = mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(ordersResponse).get(0).get("id").asLong();

        // Para poder pagar, pasamos el flujo de estados
        // Forzar estado de la comanda a ALL_DELIVERED para poder emitir precuenta
        com.pezbackend.orders.domain.model.aggregates.Order orderEntity = orderRepository.findById(orderId).orElseThrow();
        orderEntity.setStatus(com.pezbackend.orders.domain.model.valueobjects.OrderStatus.ALL_DELIVERED);
        orderRepository.saveAndFlush(orderEntity);

        // Primero emitimos la precuenta (issue-receipt)
        mockMvc.perform(post("/api/v1/orders/" + orderId + "/issue-receipt")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Y pagamos llamando directamente al servicio de comandos
        orderCommandService.markAsPaid(orderId);

        // Verificar liberación automática y limpieza
        assertEquals(TableStatus.FREE, restaurantTableRepository.findById(t1).orElseThrow().getStatus());
        
        RestaurantTable table2AfterRelease = restaurantTableRepository.findById(t2).orElseThrow();
        assertEquals(TableStatus.FREE, table2AfterRelease.getStatus());
        assertNull(table2AfterRelease.getAnchorTableId());

        // 8. Test deshacer fusión manual antes de comandar (cuando está FREE)
        mockMvc.perform(post("/api/v1/tables/" + t1 + "/merge")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mergeResource)))
                .andExpect(status().isNoContent());

        // Deshacer fusión
        mockMvc.perform(post("/api/v1/tables/" + t1 + "/unmerge")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        assertNull(restaurantTableRepository.findById(t2).orElseThrow().getAnchorTableId());
    }

    @Test
    public void testOrderTransferBetweenTables() throws Exception {
        // 1. Crear mesa de origen y destino
        Long fromTable = createTable(200, tokenA);
        Long toTable = createTable(201, tokenA);

        // 2. Iniciar pedido en la mesa de origen
        mockMvc.perform(post("/api/v1/tables/" + fromTable + "/request-attention")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/tables/" + fromTable + "/attend")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        assertEquals(TableStatus.TAKING_ORDER, restaurantTableRepository.findById(fromTable).orElseThrow().getStatus());
        assertEquals(TableStatus.FREE, restaurantTableRepository.findById(toTable).orElseThrow().getStatus());

        // 3. Trasladar pedido de fromTable a toTable
        TransferOrderResource transferResource = new TransferOrderResource(toTable);
        mockMvc.perform(post("/api/v1/tables/" + fromTable + "/transfer-order")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferResource)))
                .andExpect(status().isNoContent());

        // Verificar estados resultantes
        assertEquals(TableStatus.FREE, restaurantTableRepository.findById(fromTable).orElseThrow().getStatus());
        assertEquals(TableStatus.TAKING_ORDER, restaurantTableRepository.findById(toTable).orElseThrow().getStatus());

        // 4. Intentar trasladar a una mesa ocupada -> 409 Conflict o similar
        Long occupiedTable = createTable(202, tokenA);
        mockMvc.perform(post("/api/v1/tables/" + occupiedTable + "/request-attention")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/tables/" + toTable + "/transfer-order")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferOrderResource(occupiedTable))))
                .andExpect(status().isConflict());

        // 5. Intentar trasladar desde una mesa ancla con fusiones activas -> debe ser rechazado
        Long anchor = createTable(203, tokenA);
        Long merged = createTable(204, tokenA);

        // Fusionar
        mockMvc.perform(post("/api/v1/tables/" + anchor + "/merge")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MergeTablesResource(List.of(merged)))))
                .andExpect(status().isNoContent());

        // Iniciar pedido en anchor
        mockMvc.perform(post("/api/v1/tables/" + anchor + "/request-attention")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Intentar trasladar
        mockMvc.perform(post("/api/v1/tables/" + anchor + "/transfer-order")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferOrderResource(toTable))))
                .andExpect(status().isConflict());
    }

    @Test
    public void testReservationsFlowAndTenantIsolation() throws Exception {
        Long tableA = createTable(300, tokenA);

        // 1. Crear Reserva en Tenant A
        LocalDateTime dateTime = LocalDateTime.now().plusDays(2);
        CreateReservationResource resA = new CreateReservationResource(
                "Familia Gomez", "999888777", null, dateTime, 4, "Cerca a la ventana", tableA
        );

        String responseRes = mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("Familia Gomez"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn().getResponse().getContentAsString();

        Long resId = objectMapper.readTree(responseRes).get("id").asLong();

        // 2. Consulta de reservas en Tenant A -> debe aparecer la reserva
        mockMvc.perform(get("/api/v1/reservations")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Familia Gomez"));

        // 3. Aislamiento por Tenant: Consulta de reservas en Tenant B -> debe estar vacío
        mockMvc.perform(get("/api/v1/reservations")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // 4. Intentar actualizar o cancelar la reserva de Tenant A desde Tenant B -> debe arrojar 404
        mockMvc.perform(post("/api/v1/reservations/" + resId + "/cancel")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // 5. Completar reserva en Tenant A
        mockMvc.perform(post("/api/v1/reservations/" + resId + "/complete")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/reservations")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }
}
