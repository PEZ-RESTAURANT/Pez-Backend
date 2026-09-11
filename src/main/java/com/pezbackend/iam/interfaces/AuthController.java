package com.pezbackend.iam.interfaces;

import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.PasswordResetToken;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.pezbackend.shared.infrastructure.email.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final HashingService hashingService;
    private final com.pezbackend.shared.infrastructure.notification.EmailNotificationChannel emailNotificationChannel;

    public AuthController(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            @org.springframework.beans.factory.annotation.Qualifier("hashingServiceImpl") HashingService hashingService,
            com.pezbackend.shared.infrastructure.notification.EmailNotificationChannel emailNotificationChannel
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.hashingService = hashingService;
        this.emailNotificationChannel = emailNotificationChannel;
    }

    public void resetRateLimits() {
        emailRateLimits.clear();
        ipRateLimits.clear();
    }

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // Rate Limiting caches: ConcurrentHashMap tracking timestamps per email and IP
    private final Map<String, List<Instant>> emailRateLimits = new ConcurrentHashMap<>();
    private final Map<String, List<Instant>> ipRateLimits = new ConcurrentHashMap<>();

    public record ForgotPasswordRequest(
            @NotBlank(message = "El correo electrónico es requerido.")
            @Email(message = "Formato de correo electrónico inválido.")
            String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank(message = "El token es requerido.")
            String token,
            @NotBlank(message = "La nueva contraseña es requerida.")
            String newPassword
    ) {}

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest requestBody,
            HttpServletRequest request
    ) {
        String email = requestBody.email().trim().toLowerCase();
        String ip = getClientIp(request);

        log.info("Solicitud de recuperación de contraseña para: {} desde IP: {}", email, ip);

        // 1. Verificar Rate Limiting antes que nada para prevenir timing attacks y enumeración
        if (isRateLimited(email, ip)) {
            log.warn("Límite de solicitudes excedido para: {} o IP: {}", email, ip);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Demasiadas solicitudes. Por favor, intente de nuevo en 10 minutos.");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        // 2. Buscar usuario. Siempre responder el mismo mensaje genérico para seguridad
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Generar token aleatorio largo
            String rawToken = UUID.randomUUID().toString().replace("-", "") + 
                              UUID.randomUUID().toString().replace("-", "");
            String tokenHash = hashToken(rawToken);

            // Guardar token en base de datos
            PasswordResetToken resetToken = new PasswordResetToken(
                    user,
                    tokenHash,
                    LocalDateTime.now().plusMinutes(30)
            );
            passwordResetTokenRepository.save(resetToken);

            // Enviar correo electrónico
            String resetUrl = frontendUrl + "/auth/reset-password?token=" + rawToken;
            sendResetEmail(user.getEmail(), user.getFirstName(), resetUrl);
        } else {
            // Mitigación de timing attack: simular retardo artificial si el usuario no existe
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Si el correo electrónico ingresado existe en nuestro sistema, recibirá un enlace para restablecer su contraseña.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest requestBody
    ) {
        String rawToken = requestBody.token();
        String newPassword = requestBody.newPassword();

        String tokenHash = hashToken(rawToken);
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            return errorResponse("El enlace de recuperación es inválido o ya ha sido utilizado.");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isUsed()) {
            return errorResponse("Este enlace de recuperación ya ha sido utilizado.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return errorResponse("El enlace de recuperación ha expirado. Por favor, solicite uno nuevo.");
        }

        // Cambiar la contraseña del usuario
        User user = resetToken.getUser();
        String newPasswordHash = hashingService.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        
        // Registrar fecha de cambio de contraseña para revocar automáticamente tokens JWT existentes
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Enviar correo de confirmación de cambio exitoso
        sendConfirmationEmail(user.getEmail(), user.getFirstName());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña restablecida exitosamente.");
        return ResponseEntity.ok(response);
    }

    public record VerifyEmailRequest(
            @jakarta.validation.constraints.NotBlank(message = "El token es obligatorio.")
            String token
    ) {}

    public record ResendVerificationRequest(
            @jakarta.validation.constraints.NotBlank(message = "El correo electrónico es obligatorio.")
            @jakarta.validation.constraints.Email(message = "Debe ser un correo electrónico válido.")
            String email
    ) {}

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        String rawToken = request.token();
        String tokenHash = hashToken(rawToken);
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            return errorResponse("El enlace de verificación es inválido o ya ha sido utilizado.");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isUsed()) {
            return errorResponse("Este enlace de verificación ya ha sido utilizado.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return errorResponse("El enlace de verificación ha expirado. Por favor, solicite uno nuevo.");
        }

        User user = resetToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cuenta verificada exitosamente.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        String email = request.email().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Si el correo electrónico existe en nuestro sistema y está pendiente de verificación, se enviará un nuevo enlace.");
            return ResponseEntity.ok(response);
        }

        User user = userOpt.get();
        if (user.isVerified()) {
            return errorResponse("Esta cuenta ya está verificada. Por favor, inicie sesión normalmente.");
        }

        String rawToken = UUID.randomUUID().toString().replace("-", "") + 
                          UUID.randomUUID().toString().replace("-", "");
        String tokenHash = hashToken(rawToken);

        PasswordResetToken verificationToken = new PasswordResetToken(
                user,
                tokenHash,
                LocalDateTime.now().plusHours(24)
        );
        passwordResetTokenRepository.save(verificationToken);

        String verifyUrl = frontendUrl + "/auth/verify-email?token=" + rawToken;
        sendVerificationEmail(user.getEmail(), user.getFirstName(), verifyUrl);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Se ha enviado un nuevo enlace de verificación a su correo electrónico.");
        return ResponseEntity.ok(response);
    }

    private void sendVerificationEmail(String to, String firstName, String verifyUrl) {
        Map<String, Object> model = Map.of(
            "title", "Verificar tu cuenta de correo",
            "subtitle", "Sistema de Gestión Al Toque",
            "greeting", "Hola, " + firstName + ":",
            "paragraphs", List.of(
                "¡Gracias por registrar tu restaurante en Al Toque! Antes de comenzar, por favor confirma tu cuenta de correo electrónico.",
                "Haz clic en el siguiente botón para verificar tu cuenta:"
            ),
            "buttonText", "Verificar Cuenta",
            "buttonUrl", verifyUrl,
            "isSuccess", true,
            "alertText", "Este enlace de verificación es de un solo uso y expirará en 24 horas."
        );
        emailNotificationChannel.send(to, "Verifica tu cuenta de correo en Al Toque", "email-template", model);
    }

    private ResponseEntity<Map<String, String>> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private boolean isRateLimited(String email, String ip) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(java.time.Duration.ofMinutes(10));

        // Limitar por email: máximo 3 solicitudes en 10 minutos
        List<Instant> emailTimes = emailRateLimits.computeIfAbsent(email, k -> new java.util.concurrent.CopyOnWriteArrayList<>());
        emailTimes.removeIf(time -> time.isBefore(windowStart));
        if (emailTimes.size() >= 3) {
            return true;
        }

        // Limitar por IP: máximo 3 solicitudes en 10 minutos
        List<Instant> ipTimes = ipRateLimits.computeIfAbsent(ip, k -> new java.util.concurrent.CopyOnWriteArrayList<>());
        ipTimes.removeIf(time -> time.isBefore(windowStart));
        if (ipTimes.size() >= 3) {
            return true;
        }

        emailTimes.add(now);
        ipTimes.add(now);
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String hashToken(String rawToken) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear el token", e);
        }
    }

    private void sendResetEmail(String to, String firstName, String resetUrl) {
        Map<String, Object> model = Map.of(
            "title", "Restablecer Contraseña",
            "subtitle", "Sistema de Gestión Al Toque",
            "greeting", "Hola, " + firstName + ":",
            "paragraphs", List.of(
                "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta de empleado de Al Toque.",
                "Haz clic en el siguiente botón para continuar:"
            ),
            "buttonText", "Restablecer Contraseña",
            "buttonUrl", resetUrl,
            "isWarning", true,
            "alertText", "Este enlace es de un solo uso y expirará en 30 minutos. Si no solicitaste este cambio, puedes ignorar este correo de forma segura."
        );
        emailNotificationChannel.send(to, "Restablecer tu contraseña en Al Toque", "email-template", model);
    }

    private void sendConfirmationEmail(String to, String firstName) {
        Map<String, Object> model = Map.of(
            "title", "Contraseña Cambiada",
            "subtitle", "Sistema de Gestión Al Toque",
            "greeting", "Hola, " + firstName + ":",
            "paragraphs", List.of(
                "Te informamos que la contraseña de tu cuenta de empleado de Al Toque ha sido restablecida exitosamente.",
                "Si realizaste este cambio, puedes ignorar este correo."
            ),
            "isSuccess", true,
            "alertTitle", "⚠️ IMPORTANTE:",
            "alertText", "Si tú NO solicitaste ni realizaste este cambio, por favor contacta de inmediato con el administrador del sistema."
        );
        emailNotificationChannel.send(to, "Tu contraseña ha sido restablecida - Al Toque", "email-template", model);
    }
}
