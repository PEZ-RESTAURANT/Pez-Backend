package com.pezbackend.tenancy.application.internal.commandservices;

import com.pezbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.domain.model.exceptions.RoleNotFoundException;
import com.pezbackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.exceptions.InvalidInviteCodeException;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.tenancy.domain.model.aggregates.Restaurant;
import com.pezbackend.tenancy.domain.model.commands.OnboardingCommand;
import com.pezbackend.tenancy.domain.services.RestaurantCommandService;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.PaymentMethodConfigRepository;
import com.pezbackend.billing.domain.model.entities.PaymentMethodConfig;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.pezbackend.catalog.domain.model.entities.Category;

/**
 * Implementation of {@link RestaurantCommandService} handling restaurant onboarding.
 */
@Service
@Transactional
public class RestaurantCommandServiceImpl implements RestaurantCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantCommandServiceImpl.class);

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final PaymentMethodConfigRepository paymentMethodConfigRepository;
    private final CategoryRepository categoryRepository;
    private final com.pezbackend.billing.infrastructure.persistence.jpa.repositories.BillingSequenceRepository billingSequenceRepository;

    public RestaurantCommandServiceImpl(
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            PaymentMethodConfigRepository paymentMethodConfigRepository,
            CategoryRepository categoryRepository,
            com.pezbackend.billing.infrastructure.persistence.jpa.repositories.BillingSequenceRepository billingSequenceRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.paymentMethodConfigRepository = paymentMethodConfigRepository;
        this.categoryRepository = categoryRepository;
        this.billingSequenceRepository = billingSequenceRepository;
    }

    @Value("${authorization.onboarding.invite-code}")
    private String configuredInviteCode;

    @jakarta.annotation.PostConstruct
    public void validateInviteCode() {
        if (configuredInviteCode == null || configuredInviteCode.trim().isBlank()) {
            throw new IllegalStateException("CRITICAL ERROR: 'authorization.onboarding.invite-code' property must be configured. Application cannot start without a valid invite code.");
        }
    }

    @Override
    public Restaurant handleOnboarding(OnboardingCommand command) {
        LOGGER.info("Processing onboarding for restaurant: {}", command.name());

        // Validate invite code
        if (configuredInviteCode == null || 
            command.inviteCode() == null || 
            !configuredInviteCode.trim().equalsIgnoreCase(command.inviteCode().trim())) {
            throw new InvalidInviteCodeException();
        }

        // 1. Check if admin email already exists
        if (userRepository.existsByEmail(command.adminEmail())) {
            throw new UserAlreadyExistsException(command.adminEmail());
        }

        // 3. Create and save Restaurant first to generate ID
        Restaurant restaurant = new Restaurant(
                command.name(),
                command.businessDocumentNumber(),
                command.contactEmail(),
                command.contactPhone(),
                command.address()
        );
        restaurant = restaurantRepository.save(restaurant);

        // 4. Set TenantContext manually for the current transaction BEFORE saving user or publishing events
        try {
            TenantContext.setCurrentTenantId(restaurant.getId());

            // 5. Create and save the Admin User
            String hashedPassword = hashingService.encode(command.adminPassword());
            User admin = new User(
                    command.adminEmail(),
                    hashedPassword,
                    command.adminFirstName(),
                    command.adminLastName(),
                    true // Admin is verified by default during onboarding
            );
            admin.setRestaurantId(restaurant.getId()); // Explicitly set it to ensure PrePersist doesn't overwrite with null

            var adminRole = roleRepository.findByName(Roles.ADMIN)
                    .orElseThrow(() -> new RoleNotFoundException(Roles.ADMIN));
            admin.addRole(adminRole);

            userRepository.save(admin);

            // Seed active payment methods for the new tenant
            LOGGER.info("Seeding default payment methods config for new restaurant tenant: {}", restaurant.getId());
            paymentMethodConfigRepository.save(new PaymentMethodConfig("Efectivo", PaymentMethod.CASH, true));
            paymentMethodConfigRepository.save(new PaymentMethodConfig("Tarjeta de Crédito/Débito", PaymentMethod.CARD, true));
            paymentMethodConfigRepository.save(new PaymentMethodConfig("Yape", PaymentMethod.YAPE, true));
            paymentMethodConfigRepository.save(new PaymentMethodConfig("Plin", PaymentMethod.PLIN, true));
            paymentMethodConfigRepository.save(new PaymentMethodConfig("Transferencia Bancaria", PaymentMethod.TRANSFER, true));

            // Seed active billing sequences for the new tenant
            LOGGER.info("Seeding billing sequences for new restaurant tenant: {}", restaurant.getId());
            billingSequenceRepository.save(new com.pezbackend.billing.domain.model.entities.BillingSequence(restaurant.getId(), com.pezbackend.billing.domain.model.valueobjects.DocumentType.BOLETA, 0));
            billingSequenceRepository.save(new com.pezbackend.billing.domain.model.entities.BillingSequence(restaurant.getId(), com.pezbackend.billing.domain.model.valueobjects.DocumentType.FACTURA_ELECTRONICA, 0));

            LOGGER.info("Onboarding completed: Restaurant {} (ID: {}) and Admin User {} created", 
                    restaurant.getName(), restaurant.getId(), admin.getEmail());

        } finally {
            TenantContext.clear();
        }

        return restaurant;
    }
}
