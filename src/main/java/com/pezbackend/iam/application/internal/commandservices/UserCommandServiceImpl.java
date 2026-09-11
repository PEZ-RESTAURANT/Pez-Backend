package com.pezbackend.iam.application.internal.commandservices;

import com.pezbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.commands.SignInCommand;
import com.pezbackend.iam.domain.model.commands.SignUpCommand;
import com.pezbackend.iam.domain.model.commands.UpdateUserCommand;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.exceptions.InvalidCredentialsException;
import com.pezbackend.iam.domain.model.exceptions.RoleNotFoundException;
import com.pezbackend.iam.domain.model.exceptions.UserAccountDeactivatedException;
import com.pezbackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.pezbackend.iam.domain.model.exceptions.UserNotFoundException;
import com.pezbackend.iam.domain.services.RoleValidationService;
import com.pezbackend.iam.domain.services.UserCommandService;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.infrastructure.notification.EmailNotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of UserCommandService
 * <p>
 * This service handles command-based operations for the User aggregate.
 * It implements the ItemCommandService interface and provides business logic
 * for user registration and authentication.
 * </p>
 */
@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleValidationService roleValidationService;
    private final EmailNotificationChannel emailNotificationChannel;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleValidationService roleValidationService,
            EmailNotificationChannel emailNotificationChannel) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleValidationService = roleValidationService;
        this.emailNotificationChannel = emailNotificationChannel;
    }

    @Override
    public User handle(SignUpCommand command) {
        LOGGER.info("Processing SignUp command for email: {} with role: {}",
            command.email(), command.requestedRole());

        // Validate if user exists
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        // Validate password size
        if (command.password() == null || command.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Hash the password
        String hashedPassword = hashingService.encode(command.password());

        // Create user
        User user = new User(
            command.email(),
            hashedPassword,
            command.firstName(),
            command.lastName(),
            true // colaboradores creados por invitación empiezan verificados
        );

        // Assign role
        var roleOpt = roleRepository.findByName(command.requestedRole());
        if (roleOpt.isEmpty()) {
            throw new RoleNotFoundException(command.requestedRole());
        }
        user.addRole(roleOpt.get());

        return userRepository.save(user);
    }

    @Override
    public void handle(SignInCommand command) {
        LOGGER.info("Processing SignIn command for email: {}", command.email());

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(command.email());
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.getActive()) {
            throw new UserAccountDeactivatedException(command.email());
        }

        // Check if user is verified
        if (!user.isVerified()) {
            throw new BusinessRuleViolationException("EMAIL_NOT_VERIFIED", "Su cuenta de correo electrónico no ha sido verificada.");
        }

        // Verify password
        if (!hashingService.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        LOGGER.info("User authenticated successfully with ID: {}", user.getId());
    }

    @Override
    public User handle(UpdateUserCommand command) {
        LOGGER.info("Processing UpdateUser command for ID: {}", command.id());

        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new UserNotFoundException(command.id()));

        // Check if email is changing and already exists
        if (!user.getEmail().equalsIgnoreCase(command.email()) && userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        user.setEmail(command.email());
        user.setFirstName(command.firstName());
        user.setLastName(command.lastName());
        user.setActive(command.active());

        // Update role
        Role requestedRole = roleRepository.findByName(command.requestedRole())
                .orElseThrow(() -> new RoleNotFoundException(command.requestedRole()));

        user.getRoles().clear();
        user.addRole(requestedRole);

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        LOGGER.info("Processing changePassword command for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Verify current password
        if (!hashingService.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessRuleViolationException("INVALID_CURRENT_PASSWORD", "La contraseña actual es incorrecta.");
        }

        // Hashing the new password and save
        String newPasswordHash = hashingService.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Send email confirmation
        try {
            sendPasswordChangedEmail(user.getEmail(), user.getFirstName());
        } catch (Exception ex) {
            LOGGER.warn("No se pudo enviar el correo de confirmación de cambio de contraseña: {}", ex.getMessage());
        }
    }

    private void sendPasswordChangedEmail(String to, String firstName) {
        java.util.Map<String, Object> model = java.util.Map.of(
            "title", "Contraseña Cambiada",
            "subtitle", "Sistema de Gestión Al Toque",
            "greeting", "Hola, " + firstName + ":",
            "paragraphs", java.util.List.of(
                "Te informamos que la contraseña de tu cuenta de empleado de Al Toque ha sido cambiada exitosamente.",
                "Si realizaste este cambio, puedes ignorar este correo."
            ),
            "isSuccess", true,
            "alertTitle", "⚠️ IMPORTANTE:",
            "alertText", "Si tú NO solicitaste ni realizaste este cambio, por favor contacta de inmediato con el administrador del sistema."
        );
        emailNotificationChannel.send(to, "Tu contraseña ha sido cambiada - Al Toque", "email-template", model);
    }

    /**
     * Generate JWT token for authenticated user
     * @param user the authenticated user
     * @return JWT token string
     */
    public String generateTokenForUser(User user) {
        String userRole = user.getRoles().isEmpty() ? "CAR_OWNER" : 
                         user.getRoles().get(0).getName().name();

        return tokenService.generateToken(user.getId(), userRole, user.getRestaurantId());
    }
}
