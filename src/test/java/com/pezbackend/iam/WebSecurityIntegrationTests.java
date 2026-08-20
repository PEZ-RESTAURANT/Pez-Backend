package com.pezbackend.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class WebSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        if (roleRepository.findByName(Roles.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Roles.ADMIN));
        }
    }

    @Test
    public void testTokenRevocationAndDeactivatedUserBlocking() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String doc = String.format("%011d", (long) (Math.random() * 10000000000L));
        String email = "sec_admin_" + suffix + "@test.com";

        // 1. Onboarding del restaurante y usuario administrador
        OnboardingResource onboarding = new OnboardingResource("Sec Restaurant " + suffix, doc, "sec_info_" + suffix + "@test.com", "999888777",
                email, "password123", "Admin", "Sec", "TEST-INVITE-CODE");

        String onboardingResponse = mockMvc.perform(post("/api/v1/restaurants/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboarding)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long tenantId = objectMapper.readTree(onboardingResponse).get("id").asLong();

        User user = userRepository.findByEmail(email).orElseThrow();
        String token = tokenService.generateToken(user.getId(), Roles.ADMIN.name(), tenantId);

        // 2. Verificar que el token funciona correctamente y da acceso a endpoints protegidos
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. Hacer Sign Out (Logout) para revocar el token
        mockMvc.perform(post("/api/v1/users/signout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 4. Verificar que el token revocado ya no tiene acceso (devuelve 401 Unauthorized)
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        // 5. Crear un nuevo token para el mismo usuario (simulando un nuevo login)
        Thread.sleep(1000);
        String newToken = tokenService.generateToken(user.getId(), Roles.ADMIN.name(), tenantId);

        // Confirmamos que el nuevo token funciona
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());

        // 6. Desactivar la cuenta del usuario en base de datos
        user.setActive(false);
        userRepository.saveAndFlush(user);

        // 7. Confirmar que el token activo se rechaza inmediatamente debido a que la cuenta está inactiva (devuelve 403 Forbidden)
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isForbidden());
    }
}
