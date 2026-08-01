package com.pezbackend.shared;

import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.domain.model.DomainEvent;
import com.pezbackend.shared.infrastructure.DomainEventListener;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para validar el comportamiento de la infraestructura compartida (Fase 0).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SharedInfrastructureTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DomainEventListener domainEventListener;

    @Autowired
    private AuditEventRepository auditEventRepository;

    /**
     * Registro de prueba que implementa {@link DomainEvent} para validar la infraestructura de auditoría.
     */
    public record MockDomainEvent(
            String eventType,
            String module,
            String userId,
            String deviceId,
            Map<String, Object> payload,
            String reason,
            LocalDateTime timestamp
    ) implements DomainEvent {}

    @Test
    public void testResourceNotFoundMapping() throws Exception {
        mockMvc.perform(get("/test/not-found")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TEST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Resource test not found"))
                .andExpect(jsonPath("$.details.key").value("val"));
    }

    @Test
    public void testBusinessRuleViolationMapping() throws Exception {
        mockMvc.perform(get("/test/business-violation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TEST_BUSINESS_VIOLATION"));
    }

    @Test
    public void testPermissionDeniedMapping() throws Exception {
        mockMvc.perform(get("/test/permission-denied")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("TEST_PERMISSION_DENIED"));
    }

    @Test
    public void testStateTransitionMapping() throws Exception {
        mockMvc.perform(get("/test/state-transition")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TEST_STATE_TRANSITION"));
    }

    @Test
    public void testDomainEventListenerAndPersistDirectly() {
        auditEventRepository.deleteAll();

        MockDomainEvent event = new MockDomainEvent(
                "MockEvent",
                "testing",
                "admin_test",
                "device_123",
                Map.of("param", "value"),
                "automated test reason",
                LocalDateTime.now()
        );

        // Invocar directamente el listener para probar su lógica interna de mapeo y guardado
        domainEventListener.handleDomainEvent(event);

        List<AuditEvent> saved = auditEventRepository.findAll();
        assertThat(saved).hasSize(1);
        AuditEvent savedEvent = saved.get(0);
        assertThat(savedEvent.getEventType()).isEqualTo("MockEvent");
        assertThat(savedEvent.getModule()).isEqualTo("testing");
        assertThat(savedEvent.getUserId()).isEqualTo("admin_test");
        assertThat(savedEvent.getDeviceId()).isEqualTo("device_123");
        assertThat(savedEvent.getPayload()).containsEntry("param", "value");
        assertThat(savedEvent.getReason()).isEqualTo("automated test reason");
    }
}
