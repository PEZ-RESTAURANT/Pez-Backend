package com.pezbackend.billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.billing.domain.services.DniRucLookupService;
import com.pezbackend.billing.domain.services.LookupResult;
import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LookupIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;
    private Long tenantId;
    private User user;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public DniRucLookupService testDniRucLookupService() {
            return new DniRucLookupService() {
                @Override
                public LookupResult lookupDni(String dni) {
                    if ("12345678".equals(dni)) {
                        return new LookupResult("12345678", "JUAN PEREZ TEST", "", true);
                    }
                    return LookupResult.failed();
                }

                @Override
                public LookupResult lookupRuc(String ruc) {
                    if ("20123456789".equals(ruc)) {
                        return new LookupResult("20123456789", "EMPRESA TEST SAC", "AV. TEST 123", true);
                    }
                    return LookupResult.failed();
                }
            };
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        if (roleRepository.findByName(Roles.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Roles.ADMIN));
        }

        if (token == null) {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            String doc = String.format("%011d", (long) (Math.random() * 10000000000L));
            String email = "lookup_admin_" + suffix + "@test.com";

            OnboardingResource onboarding = new OnboardingResource("Lookup Restaurant " + suffix, doc, "lookup_info_" + suffix + "@test.com", "999888777",
                    email, "password123", "Admin", "Lookup", "TEST-INVITE-CODE");

            String onboardingResponse = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(onboarding)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            tenantId = objectMapper.readTree(onboardingResponse).get("id").asLong();
            user = userRepository.findByEmail(email).orElseThrow();
            token = tokenService.generateToken(user.getId(), Roles.ADMIN.name(), tenantId);
        }
    }

    @Test
    public void testSuccessfulDniLookupAndAuditLogging() throws Exception {
        mockMvc.perform(get("/api/v1/lookup/dni/12345678")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verificar que se registró el evento de auditoría
        List<AuditEvent> events = auditEventRepository.findAll();
        boolean found = events.stream().anyMatch(e ->
                "DniRucLookupPerformed".equals(e.getEventType()) &&
                "12345678".equals(e.getPayload().get("documentNumber")) &&
                Boolean.TRUE.equals(e.getPayload().get("success"))
        );
        assertTrue(found, "Se debería registrar un evento de auditoría DniRucLookupPerformed exitoso.");
    }

    @Test
    public void testFailedDniLookupAndAuditLogging() throws Exception {
        mockMvc.perform(get("/api/v1/lookup/dni/87654321")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // Verificar que se registró el evento de auditoría fallido
        List<AuditEvent> events = auditEventRepository.findAll();
        boolean found = events.stream().anyMatch(e ->
                "DniRucLookupPerformed".equals(e.getEventType()) &&
                "87654321".equals(e.getPayload().get("documentNumber")) &&
                Boolean.FALSE.equals(e.getPayload().get("success"))
        );
        assertTrue(found, "Se debería registrar un evento de auditoría DniRucLookupPerformed fallido.");
    }

    @Test
    public void testSuccessfulRucLookup() throws Exception {
        mockMvc.perform(get("/api/v1/lookup/ruc/20123456789")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testRateLimitingEnforcement() throws Exception {
        // Ejecutar 20 consultas seguidas
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(get("/api/v1/lookup/dni/12345678")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        // La 21-ésima consulta debería retornar 429 Too Many Requests
        mockMvc.perform(get("/api/v1/lookup/dni/12345678")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isTooManyRequests());
    }
}
