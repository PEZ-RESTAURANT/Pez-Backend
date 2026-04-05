package com.pezbackend.iam.application.internal.commandservices;

import com.pezbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.commands.SignInCommand;
import com.pezbackend.iam.domain.model.commands.SignUpCommand;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.exceptions.InvalidCredentialsException;
import com.pezbackend.iam.domain.model.exceptions.RoleNotFoundException;
import com.pezbackend.iam.domain.model.exceptions.UserAccountDeactivatedException;
import com.pezbackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.pezbackend.iam.domain.services.RoleValidationService;
import com.pezbackend.iam.domain.services.UserCommandService;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.model.exceptions.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User Command Service Implementation
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


    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleValidationService roleValidationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleValidationService = roleValidationService;
    }

    @Override
    public void handle(SignUpCommand command) {
        LOGGER.info("Processing SignUp command for email: {} with role: {}",
            command.email(), command.requestedRole());

        // Check if user already exists
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        // Validate requested role
        if (!roleValidationService.canRequestRole(command.requestedRole())) {
            throw new BusinessRuleException(
                    "Cannot request role: " + command.requestedRole()
            );
        }

        // Hash the password
        String hashedPassword = hashingService.encode(command.password());

        // Crear nuevo usuario (solo datos de IAM)
        User user = new User(
                command.email(),
                hashedPassword,
                command.firstName(),
                command.lastName(),
                false // No verificado por defecto
        );

        // Assign requested role
        Role requestedRole = roleRepository.findByName(command.requestedRole())
                .orElseThrow(() -> new RoleNotFoundException(command.requestedRole()));

        user.addRole(requestedRole);

        // Save user
        User savedUser = userRepository.save(user);
        LOGGER.info("User registered successfully with ID: {}", savedUser.getId());

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

        // Verify password
        if (!hashingService.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        LOGGER.info("User authenticated successfully with ID: {}", user.getId());
    }

    /**
     * Generate JWT token for authenticated user
     * @param user the authenticated user
     * @return JWT token string
     */
    public String generateTokenForUser(User user) {
        String userRole = user.getRoles().isEmpty() ? "CAR_OWNER" : 
                         user.getRoles().get(0).getName().name();


        return tokenService.generateToken(user.getId(), userRole);
    }
}
