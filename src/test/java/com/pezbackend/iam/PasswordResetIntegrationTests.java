package com.pezbackend.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.PasswordResetToken;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.pezbackend.iam.interfaces.AuthController.ForgotPasswordRequest;
import com.pezbackend.iam.interfaces.AuthController.ResetPasswordRequest;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PasswordResetIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("hashingServiceImpl")
    private HashingService hashingService;

    @Autowired
    private BearerTokenService tokenService;

    @Autowired
    private com.pezbackend.iam.interfaces.AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;

    @BeforeEach
    public void setUp() {
        // Reset rate limits to isolate tests
        authController.resetRateLimits();

        // Clean up previous tokens
        passwordResetTokenRepository.deleteAll();

        // Create test user if not exists
        String email = "recovery_test@pez.com";
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            testUser = existing.get();
        } else {
            testUser = new User(email, hashingService.encode("oldPassword123"), "Recovery", "Test", true);
            testUser.setRestaurantId(1L);
            testUser = userRepository.save(testUser);
        }
    }

    @Test
    public void testPasswordResetFlowEndToEnd() throws Exception {
        // 1. Solicitar restablecimiento de contraseña
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest(testUser.getEmail());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si el correo electrónico ingresado existe en nuestro sistema, recibirá un enlace para restablecer su contraseña."));

        // 2. Extraer el token guardado hasheado de la base de datos
        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();
        assertThat(tokens).isNotEmpty();
        PasswordResetToken tokenRecord = tokens.get(0);
        assertThat(tokenRecord.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(tokenRecord.isUsed()).isFalse();

        // Como no podemos obtener el token crudo (rawToken) de la base de datos porque se guardó el hash,
        // pero sabemos que en la prueba mock el emailService.sendEmail es invocado con el token,
        // podemos simplemente generar un token para simular el comportamiento.
        // O alternativamente, para probar la API, podemos simular que conocemos el rawToken.
        // En este test, usemos un token creado manualmente para asegurar que el endpoint reset-password funciona con su hash
        String fakeRawToken = "test_raw_token_value_123456789_abcdef";
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(fakeRawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String customHash = hexString.toString();

        PasswordResetToken customToken = new PasswordResetToken(testUser, customHash, LocalDateTime.now().plusMinutes(10));
        passwordResetTokenRepository.save(customToken);

        // 3. Restablecer la contraseña usando el raw token
        ResetPasswordRequest resetReq = new ResetPasswordRequest(fakeRawToken, "newSecurePassword789");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña restablecida exitosamente."));

        // 4. Verificar que se cambió la contraseña en la base de datos
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(hashingService.matches("newSecurePassword789", updatedUser.getPasswordHash())).isTrue();
        assertThat(updatedUser.getPasswordChangedAt()).isNotNull();

        // 5. Verificar que el token quedó marcado como usado y no se puede reutilizar
        PasswordResetToken usedToken = passwordResetTokenRepository.findByTokenHash(customHash).orElseThrow();
        assertThat(usedToken.isUsed()).isTrue();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Este enlace de recuperación ya ha sido utilizado."));
    }

    @Test
    public void testForgotPasswordRateLimiting() throws Exception {
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest("limit_test@pez.com");

        // Realizar 3 solicitudes exitosas
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgotReq)))
                    .andExpect(status().isOk());
        }

        // La cuarta solicitud debe ser rechazada por Rate Limiting (429)
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotReq)))
                .andExpect(status().is4xxClientError()); // Rate Limiting devuelve 429
    }
}
