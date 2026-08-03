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

    @Value("${authorization.onboarding.invite-code}")
    private String expectedInviteCode;

    public RestaurantCommandServiceImpl(
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            PaymentMethodConfigRepository paymentMethodConfigRepository,
            CategoryRepository categoryRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.paymentMethodConfigRepository = paymentMethodConfigRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Restaurant handleOnboarding(OnboardingCommand command) {
        LOGGER.info("Processing onboarding for restaurant: {}", command.name());

        // 1. Verify invitation code
        if (expectedInviteCode == null || !expectedInviteCode.equals(command.inviteCode())) {
            LOGGER.warn("Onboarding failed: invalid invite code provided");
            throw new InvalidInviteCodeException();
        }

        // 2. Check if admin email already exists
        if (userRepository.existsByEmail(command.adminEmail())) {
            throw new UserAlreadyExistsException(command.adminEmail());
        }

        // 3. Create and save Restaurant first to generate ID
        Restaurant restaurant = new Restaurant(
                command.name(),
                command.businessDocumentNumber(),
                command.contactEmail(),
                command.contactPhone()
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

            // Seed active categories for the new tenant
            LOGGER.info("Seeding default categories config for new restaurant tenant: {}", restaurant.getId());
            categoryRepository.save(new Category("Marina"));
            categoryRepository.save(new Category("Criolla"));
            categoryRepository.save(new Category("Bebidas"));
            categoryRepository.save(new Category("Bebidas Alcohólicas"));
            categoryRepository.save(new Category("Chifa"));
            categoryRepository.save(new Category("Jugos"));
            categoryRepository.save(new Category("Selva"));
            categoryRepository.save(new Category("Sopas"));
            categoryRepository.save(new Category("Parrilla"));
            categoryRepository.save(new Category("Guarniciones"));
            categoryRepository.save(new Category("Comida Rápida"));
            categoryRepository.save(new Category("Pastas"));
            categoryRepository.save(new Category("Brasa"));

            LOGGER.info("Onboarding completed: Restaurant {} (ID: {}) and Admin User {} created", 
                    restaurant.getName(), restaurant.getId(), admin.getEmail());

        } finally {
            TenantContext.clear();
        }

        return restaurant;
    }
}
