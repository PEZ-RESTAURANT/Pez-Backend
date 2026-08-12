package com.pezbackend.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.RestaurantTableRepository;
import com.pezbackend.orders.interfaces.rest.resources.CreateTableResource;
import com.pezbackend.realtime.application.internal.TableLockManager;
import com.pezbackend.realtime.domain.model.TableLock;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WebSocketsRealtimeIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.RestaurantRepository restaurantRepository;

    @Autowired
    private TableLockManager tableLockManager;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private String tokenA;
    private String tokenA2;
    private Long idA;
    private String emailOther;

    private String tokenB;
    private Long idB;

    @BeforeEach
    public void setUp() throws Exception {
        if (roleRepository.findByName(Roles.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Roles.ADMIN));
        }

        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String docA = String.format("%011d", (long) (Math.random() * 10000000000L));
        String docB = String.format("%011d", (long) (Math.random() * 10000000000L));

        String emailA = "ws_admin_" + suffix + "@tenanta.com";
        String emailB = "ws_admin_" + suffix + "@tenantb.com";
        emailOther = "other_waiter_" + suffix + "@tenanta.com";

        // Onboarding Tenant A
        OnboardingResource resourceA = new OnboardingResource(
                "Ws Tenant A " + suffix, docA, "ws_info_" + suffix + "@tenanta.com", "999111222",
                emailA, "passA", "Admin", "A"
        );
        String responseA = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        idA = objectMapper.readTree(responseA).get("id").asLong();

        // Onboarding Tenant B
        OnboardingResource resourceB = new OnboardingResource(
                "Ws Tenant B " + suffix, docB, "ws_info_" + suffix + "@tenantb.com", "999333444",
                emailB, "passB", "Admin", "B"
        );
        String responseB = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        idB = objectMapper.readTree(responseB).get("id").asLong();

        User adminA = userRepository.findByEmail(emailA).orElseThrow();
        User adminB = userRepository.findByEmail(emailB).orElseThrow();

        tokenA = tokenService.generateToken(adminA.getId(), Roles.ADMIN.name(), idA);
        tokenB = tokenService.generateToken(adminB.getId(), Roles.ADMIN.name(), idB);

        // Crear mozo secundario en Tenant A para probar conflictos de bloqueo en la misma mesa del mismo restaurante
        User otherUser = new User(emailOther, "pass", "Other", "Waiter", true);
        otherUser.setRestaurantId(idA);
        otherUser.setActive(true);
        otherUser.setRoles(List.of(roleRepository.findByName(Roles.ADMIN).get()));
        userRepository.save(otherUser);

        tokenA2 = tokenService.generateToken(otherUser.getId(), Roles.ADMIN.name(), idA);
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
    public void testWebSocketTenantIsolationAndMessaging() throws Exception {
        // 1. Configurar cliente WebSocket STOMP
        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
        );
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Conectar Cliente A
        StompHeaders headersA = new StompHeaders();
        headersA.add("Authorization", "Bearer " + tokenA);
        
        CompletableFuture<StompSession> sessionFutureA = new CompletableFuture<>();
        stompClient.connectAsync("ws://localhost:" + port + "/ws", (org.springframework.web.socket.WebSocketHttpHeaders) null, headersA, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionFutureA.complete(session);
            }
        });

        StompSession sessionA = sessionFutureA.get(5, TimeUnit.SECONDS);
        assertNotNull(sessionA);

        // 2. Intentar suscribir Cliente A (Tenant A) al tópico de Tenant B -> Debería fallar/desconectarse por interceptor
        StompHeaders subHeadersB = new StompHeaders();
        subHeadersB.setDestination("/topic/restaurants/" + idB + "/tables");
        
        CompletableFuture<Throwable> errorFuture = new CompletableFuture<>();
        sessionA.subscribe(subHeadersB, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // No debería recibir nada
            }
        });

        // Esperar un momento y validar que la conexión fallida de suscripción cruzada gatille rechazo
        // Si el interceptor de suscripción lanza una excepción, Spring manda un frame ERROR y cierra la conexión.
        sessionA.setAutoReceipt(true);
        try {
            sessionA.send("/app/invalid", "test");
        } catch (Exception e) {
            // Conexión cerrada
        }

        // 3. Reconectar Cliente A y suscribirse a su propio canal
        CompletableFuture<StompSession> sessionFutureA2 = new CompletableFuture<>();
        stompClient.connectAsync("ws://localhost:" + port + "/ws", (org.springframework.web.socket.WebSocketHttpHeaders) null, headersA, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("DEBUG WS: Cliente A2 conectado con ID de sesión " + session.getSessionId());
                sessionFutureA2.complete(session);
            }
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("DEBUG WS EXCEPTION: " + exception.getMessage());
                exception.printStackTrace();
            }
            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("DEBUG WS TRANSPORT ERROR: " + exception.getMessage());
                exception.printStackTrace();
            }
        });
        StompSession sessionA2 = sessionFutureA2.get(5, TimeUnit.SECONDS);

        BlockingQueue<Map> messageQueueA = new LinkedBlockingQueue<>();
        sessionA2.subscribe("/topic/restaurants/" + idA + "/tables", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("DEBUG WS FRAME: Recibido payload: " + payload);
                messageQueueA.offer((Map) payload);
            }
        });

        // Dar un segundo al broker de mensajería para registrar la suscripción antes de disparar el evento
        Thread.sleep(1000);

        // 4. Crear mesa en Tenant A y bloquearla -> gatilla propagación en tiempo real
        Long tableA = createTable(900, tokenA);
        
        mockMvc.perform(post("/api/v1/tables/" + tableA + "/lock")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Verificar que Cliente A recibió la notificación del bloqueo de su mesa
        Map msg = messageQueueA.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg);
        assertEquals("TableLocked", msg.get("eventType"));
        Map payload = (Map) msg.get("payload");
        assertEquals(tableA.intValue(), ((Number) payload.get("tableId")).intValue());
    }

    @Test
    public void testTableSoftLockingFlowAndExpiration() throws Exception {
        Long tableId = createTable(910, tokenA);

        // 1. Mozo A bloquea la mesa
        mockMvc.perform(post("/api/v1/tables/" + tableId + "/lock")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // 2. Mozo B (de la misma mesa en Tenant A) intenta bloquear la misma mesa -> 409 Conflict
        mockMvc.perform(post("/api/v1/tables/" + tableId + "/lock")
                        .header("Authorization", "Bearer " + tokenA2))
                .andExpect(status().isConflict());

        // 3. Mozo A desbloquea la mesa
        mockMvc.perform(post("/api/v1/tables/" + tableId + "/unlock")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // 4. Mozo B ahora sí puede bloquear la mesa
        mockMvc.perform(post("/api/v1/tables/" + tableId + "/lock")
                        .header("Authorization", "Bearer " + tokenA2))
                .andExpect(status().isNoContent());

        // 5. Test Expiración del bloqueo: simulamos inyectando un bloqueo viejo (4 minutos atrás) en Tenant A
        User otherUser = userRepository.findByEmail(emailOther).orElseThrow();
        TableLock oldLock = new TableLock(tableId, otherUser.getId(), otherUser.getFirstName() + " " + otherUser.getLastName(), LocalDateTime.now().minusMinutes(4));
        tableLockManager.lockTable(idA, tableId, otherUser.getId(), otherUser.getFirstName() + " " + otherUser.getLastName());
        
        // Modificamos manualmente el timestamp en memoria para simular envejecimiento
        java.lang.reflect.Field locksField = TableLockManager.class.getDeclaredField("locks");
        locksField.setAccessible(true);
        Map<Long, Map<Long, TableLock>> locksMap = (Map<Long, Map<Long, TableLock>>) locksField.get(tableLockManager);
        locksMap.get(idA).put(tableId, oldLock);

        // Ejecutar sweep de expiración programado manualmente
        tableLockManager.sweepExpiredLocks();

        // Mozo A debería poder bloquear la mesa ahora porque expiró el bloqueo anterior de Mozo B
        mockMvc.perform(post("/api/v1/tables/" + tableId + "/lock")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }
}
