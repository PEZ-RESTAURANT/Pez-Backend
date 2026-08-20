package com.pezbackend.staff.interfaces.rest;

import com.pezbackend.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.commands.SignUpCommand;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.shared.infrastructure.notification.EmailNotificationChannel;
import com.pezbackend.staff.domain.model.aggregates.StaffInvite;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.StaffInviteRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.staff.interfaces.rest.resources.CreateStaffInviteResource;
import com.pezbackend.staff.interfaces.rest.resources.AcceptInviteResource;
import com.pezbackend.staff.interfaces.rest.resources.StaffInviteResource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para el manejo de invitaciones de personal.
 */
@RestController
@RequestMapping("/api/v1/staff/invites")
@RequiredArgsConstructor
public class StaffInviteController {

    private final StaffInviteRepository staffInviteRepository;
    private final UserCommandServiceImpl userCommandService;
    private final UserRepository userRepository;
    private final EmailNotificationChannel emailNotificationChannel;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * POST /api/v1/staff/invites
     * Generar una invitación de personal (rol asignado, expiración 48 horas).
     */
    @PostMapping
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<?> createInvite(@Valid @RequestBody CreateStaffInviteResource resource) {
        String roleStr = resource.requestedRole().toUpperCase();
        if (!roleStr.equals("WAITER") && !roleStr.equals("COOK") && !roleStr.equals("CASHIER")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Solo se permiten roles WAITER, COOK y CASHIER."));
        }

        String email = resource.email().trim().toLowerCase();

        // 1. Validar que no exista un usuario registrado con este correo
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El correo electrónico ya está registrado como usuario."));
        }

        // 2. Validar que no exista una invitación activa pendiente para este correo
        Optional<StaffInvite> activeInviteOpt = staffInviteRepository.findFirstByEmailAndUsedFalseAndRevokedFalse(email);
        if (activeInviteOpt.isPresent() && !activeInviteOpt.get().isExpired()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe una invitación activa y pendiente para este correo electrónico."));
        }

        // Expiración por defecto de 48 horas
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(48);

        // Generar un código único corto de 8 caracteres
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        while (staffInviteRepository.findByCode(code).isPresent()) {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }

        StaffInvite invite = new StaffInvite(code, roleStr, email, expiresAt);
        StaffInvite savedInvite = staffInviteRepository.save(invite);

        // Enviar notificación por correo
        String inviteUrl = frontendUrl + "/join/" + code;
        sendInviteEmail(email, roleStr, inviteUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResource(savedInvite));
    }

    /**
     * GET /api/v1/staff/invites
     * Listado de invitaciones del restaurante actual (filtrado por tenantFilter).
     */
    @GetMapping
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<List<StaffInviteResource>> getAllInvites() {
        List<StaffInviteResource> invites = staffInviteRepository.findAll().stream()
                .map(this::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invites);
    }

    /**
     * DELETE /api/v1/staff/invites/{code}
     * Revocar una invitación antes de que se use.
     */
    @DeleteMapping("/{code}")
    @RequiresPermission("staff.manage_employees")
    public ResponseEntity<?> revokeInvite(@PathVariable String code) {
        Optional<StaffInvite> inviteOpt = staffInviteRepository.findByCode(code);
        if (inviteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StaffInvite invite = inviteOpt.get();
        invite.setRevoked(true);
        staffInviteRepository.save(invite);

        return ResponseEntity.ok(Map.of("message", "Invitación revocada con éxito."));
    }

    /**
     * GET /api/v1/staff/invites/{code}
     * Público: Validar si un código de invitación existe y no ha expirado.
     */
    @GetMapping("/{code}")
    public ResponseEntity<?> verifyInvite(@PathVariable String code) {
        Optional<StaffInvite> inviteOpt = staffInviteRepository.findByCode(code);
        if (inviteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "La invitación no existe o es inválida."));
        }

        StaffInvite invite = inviteOpt.get();
        if (!invite.isValid()) {
            String message = "La invitación ha expirado, ha sido usada o revocada.";
            if (invite.isUsed()) {
                message = "Esta invitación ya ha sido utilizada.";
            } else if (invite.isRevoked()) {
                message = "Esta invitación ha sido revocada.";
            } else if (invite.isExpired()) {
                message = "Esta invitación ha expirado.";
            }
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", message));
        }

        return ResponseEntity.ok(toResource(invite));
    }

    /**
     * POST /api/v1/staff/invites/{code}/accept
     * Público: Aceptar invitación y crear cuenta de colaborador.
     */
    @PostMapping("/{code}/accept")
    public ResponseEntity<?> acceptInvite(@PathVariable String code, @Valid @RequestBody AcceptInviteResource resource) {
        Optional<StaffInvite> inviteOpt = staffInviteRepository.findByCode(code);
        if (inviteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "La invitación no existe."));
        }

        StaffInvite invite = inviteOpt.get();
        if (!invite.isValid()) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "La invitación no es válida o ya expiró."));
        }

        try {
            // Establecer el TenantContext del restaurante de la invitación
            TenantContext.setCurrentTenantId(invite.getRestaurantId());

            SignUpCommand signUpCommand = new SignUpCommand(
                    invite.getEmail(),
                    resource.password(),
                    resource.firstName(),
                    resource.lastName(),
                    Roles.valueOf(invite.getRequestedRole())
            );

            userCommandService.handle(signUpCommand);

            // Marcar invitación como usada
            invite.setUsed(true);
            staffInviteRepository.save(invite);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Cuenta de colaborador creada con éxito."));
        } catch (com.pezbackend.iam.domain.model.exceptions.UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "El correo electrónico ya está registrado."));
        } finally {
            TenantContext.clear();
        }
    }

    private void sendInviteEmail(String to, String role, String inviteUrl) {
        Map<String, Object> model = Map.of(
            "title", "Invitación al equipo de Al Toque",
            "subtitle", "Registra tu nueva cuenta de colaborador",
            "greeting", "¡Hola!",
            "paragraphs", List.of(
                "Has sido invitado a unirte a nuestro equipo de trabajo con el rol de " + role + ".",
                "Para completar tu registro, establecer tu propia contraseña y comenzar a usar el sistema, por favor haz clic en el siguiente botón:"
            ),
            "buttonText", "Completar Registro",
            "buttonUrl", inviteUrl,
            "isWarning", false,
            "alertText", "Este enlace de invitación expirará en 48 horas y solo puede ser utilizado una vez."
        );
        emailNotificationChannel.send(to, "Invitación para unirte al equipo de Al Toque", "email-template", model);
    }

    private StaffInviteResource toResource(StaffInvite invite) {
        return new StaffInviteResource(
                invite.getId(),
                invite.getCode(),
                invite.getRequestedRole(),
                invite.getEmail(),
                invite.getExpiresAt().toString(),
                invite.isUsed(),
                invite.isRevoked(),
                invite.isExpired(),
                invite.getRestaurantId()
        );
    }
}
