package io.github.rafaviv.panther.pantherbackend.iam.interfaces;

import io.github.rafaviv.panther.pantherbackend.iam.application.internal.commandservices.UserCommandServiceImpl;
import io.github.rafaviv.panther.pantherbackend.iam.application.internal.queryservices.UserQueryServiceImpl;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.commands.SignInCommand;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.commands.SignUpCommand;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.exceptions.InvalidCredentialsException;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.exceptions.UserAccountDeactivatedException;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.exceptions.UserNotFoundException;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.queries.GetAllUsersQuery;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.queries.GetUserByEmailQuery;
import io.github.rafaviv.panther.pantherbackend.iam.domain.services.RoleValidationService;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.resources.AuthenticationResponseResource;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.resources.SignInResource;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.resources.SignUpResource;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.resources.UserResource;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import io.github.rafaviv.panther.pantherbackend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Users REST Controller
 * <p>
 * This controller handles HTTP requests for user-related operations including
 * registration, authentication, and user management following REST principles.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    private final UserCommandServiceImpl userCommandService;
    private final UserQueryServiceImpl userQueryService;
    private final RoleValidationService roleValidationService;

    public UsersController(
            UserCommandServiceImpl userCommandService,
            UserQueryServiceImpl userQueryService,
            RoleValidationService roleValidationService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.roleValidationService = roleValidationService;
    }

    /**
     * Register a new user
     * @param signUpResource the user registration data
     * @return ResponseEntity with success message
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpResource signUpResource) {
        try {
            System.out.println("🚀 Entrando al endpoint /signup");
            LOGGER.info("Processing signup request for email: {}", signUpResource.email());
            
            SignUpCommand command = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
            userCommandService.handle(command);
            
            LOGGER.info("User registered successfully: {}", signUpResource.email());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User registered successfully");
                    
        } catch (UserAlreadyExistsException e) {
            LOGGER.warn("Signup failed for email {}: {}", signUpResource.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Signup failed for email {}: {}", signUpResource.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during signup for email {}: {}", signUpResource.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during registration");
        }
    }

    /**
     * Authenticate a user and return JWT token
     * @param signInResource the user authentication data
     * @return ResponseEntity with JWT token and user information
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource signInResource) {
        try {
            LOGGER.info("Processing signin request for email: {}", signInResource.email());
            
            SignInCommand command = SignInCommandFromResourceAssembler.toCommandfromResource(signInResource);
            userCommandService.handle(command);
            
            // Get user details for token generation
            Optional<User> userOptional = userQueryService.handle(new GetUserByEmailQuery(signInResource.email()));
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication failed");
            }
            
            User user = userOptional.get();
            String token = userCommandService.generateTokenForUser(user);
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
            
            // Token expires in 7 days (604800 seconds)
            AuthenticationResponseResource response = AuthenticationResponseResource.of(token, 604800L, userResource);
            
            LOGGER.info("User authenticated successfully: {}", signInResource.email());
            return ResponseEntity.ok(response);
                    
        } catch (InvalidCredentialsException e) {
            LOGGER.warn("Signin failed for email {}: {}", signInResource.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (UserAccountDeactivatedException e) {
            LOGGER.warn("Signin failed for email {}: {}", signInResource.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Signin failed for email {}: {}", signInResource.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during signin for email {}: {}", signInResource.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during authentication");
        }
    }

    /**
     * Get user by email
     * @param email the user email
     * @return ResponseEntity with user information
     */
    @GetMapping("/by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            LOGGER.debug("Processing getUserByEmail request for email: {}", email);
            
            Optional<User> userOptional = userQueryService.handle(new GetUserByEmailQuery(email));
            
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException(email);
            }
            
            User user = userOptional.get();
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
            
            return ResponseEntity.ok(userResource);
                    
        } catch (UserNotFoundException e) {
            LOGGER.warn("User not found with email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving user by email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving user");
        }
    }

    /**
     * Get all users
     * @return ResponseEntity with list of all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            LOGGER.debug("Processing getAllUsers request");
            
            List<User> users = userQueryService.handle(new GetAllUsersQuery());
            List<UserResource> userResources = users.stream()
                    .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                    .toList();
            
            return ResponseEntity.ok(userResources);
                    
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving all users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving users");
        }
    }

    /**
     * Get available roles for registration
     * @return ResponseEntity with list of available roles
     */
    @GetMapping("/available-roles")
    public ResponseEntity<?> getAvailableRoles() {
        try {
            LOGGER.debug("Processing getAvailableRoles request");
            
            var availableRoles = roleValidationService.getAvailableRolesForRegistration();
            
            return ResponseEntity.ok(availableRoles);
                    
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving available roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving available roles");
        }
    }
}
