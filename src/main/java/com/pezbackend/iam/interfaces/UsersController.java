package com.pezbackend.iam.interfaces;

import com.pezbackend.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.pezbackend.iam.application.internal.queryservices.UserQueryServiceImpl;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.commands.SignInCommand;
import com.pezbackend.iam.domain.model.commands.SignUpCommand;
import com.pezbackend.iam.domain.model.commands.UpdateUserCommand;
import com.pezbackend.iam.domain.model.exceptions.InvalidCredentialsException;
import com.pezbackend.iam.domain.model.exceptions.UserAccountDeactivatedException;
import com.pezbackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.pezbackend.iam.domain.model.exceptions.UserNotFoundException;
import com.pezbackend.iam.domain.model.queries.GetAllUsersQuery;
import com.pezbackend.iam.domain.model.queries.GetUserByEmailQuery;
import com.pezbackend.iam.domain.services.RoleValidationService;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.iam.interfaces.rest.resources.AuthenticationResponseResource;
import com.pezbackend.iam.interfaces.rest.resources.SignInResource;
import com.pezbackend.iam.interfaces.rest.resources.SignUpResource;
import com.pezbackend.iam.interfaces.rest.resources.UpdateUserResource;
import com.pezbackend.iam.interfaces.rest.resources.UserResource;
import com.pezbackend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.pezbackend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.pezbackend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import jakarta.validation.Valid;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.shared.infrastructure.TenantContext;
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
    private final com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService tokenService;

    public UsersController(
            UserCommandServiceImpl userCommandService,
            UserQueryServiceImpl userQueryService,
            RoleValidationService roleValidationService,
            com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService tokenService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.roleValidationService = roleValidationService;
        this.tokenService = tokenService;
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
            if (user.getRestaurantId() != null && !user.getRestaurantId().equals(TenantContext.getCurrentTenantId())) {
                throw new com.pezbackend.shared.domain.exceptions.TenantMismatchException("User", user.getId());
            }

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
    @RequiresPermission("iam.manage_accounts")
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
     * Create a new staff account (admin management)
     */
    @PostMapping
    @RequiresPermission("iam.manage_accounts")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignUpResource resource) {
        try {
            LOGGER.info("Processing staff account creation request for email: {}", resource.email());
            
            SignUpCommand command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
            User user = userCommandService.handle(command);
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(userResource);
            
        } catch (UserAlreadyExistsException e) {
            LOGGER.warn("User creation failed for email {}: {}", resource.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during user creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during user creation");
        }
    }

    /**
     * Update an existing user account details or status (admin management)
     */
    @PutMapping("/{id}")
    @RequiresPermission("iam.manage_accounts")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserResource resource) {
        try {
            LOGGER.info("Processing user update request for ID: {}", id);
            
            UpdateUserCommand command = new UpdateUserCommand(
                id,
                resource.email(),
                resource.firstName(),
                resource.lastName(),
                resource.requestedRole(),
                resource.active()
            );
            
            User user = userCommandService.handle(command);
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
            
            return ResponseEntity.ok(userResource);
            
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during user update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during user update");
        }
    }

    /**
     * Get user by ID (admin management / details page)
     * @param id the user ID
     * @return ResponseEntity with user details
     */
    @GetMapping("/{id}")
    @RequiresPermission("iam.manage_accounts")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            LOGGER.debug("Processing getUserById request for ID: {}", id);
            
            User user = userQueryService.handle(new com.pezbackend.iam.domain.model.queries.GetUserByIdQuery(id))
                    .orElseThrow(() -> new UserNotFoundException(id));
            
            // Tenant Isolation
            if (user.getRestaurantId() != null && !user.getRestaurantId().equals(TenantContext.getCurrentTenantId())) {
                throw new com.pezbackend.shared.domain.exceptions.TenantMismatchException("User", id);
            }
            
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
            return ResponseEntity.ok(userResource);
            
        } catch (UserNotFoundException | com.pezbackend.shared.domain.exceptions.TenantMismatchException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving user by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving user");
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

    /**
     * Revoke current user's session token (Sign Out)
     */
    @PostMapping("/signout")
    public ResponseEntity<Void> signOut(jakarta.servlet.http.HttpServletRequest request) {
        String token = tokenService.getBearerTokenFrom(request);
        if (token != null) {
            tokenService.invalidateToken(token);
            LOGGER.info("Sesión cerrada exitosamente, token revocado.");
        }
        return ResponseEntity.ok().build();
    }
}
