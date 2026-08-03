package com.pezbackend.tenancy.interfaces.rest;

import com.pezbackend.shared.domain.exceptions.TenantMismatchException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.tenancy.domain.model.aggregates.Restaurant;
import com.pezbackend.tenancy.domain.model.commands.OnboardingCommand;
import com.pezbackend.tenancy.domain.services.RestaurantCommandService;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.RestaurantRepository;
import com.pezbackend.tenancy.interfaces.rest.resources.OnboardingResource;
import com.pezbackend.tenancy.interfaces.rest.resources.RestaurantResource;
import com.pezbackend.tenancy.interfaces.rest.transform.OnboardingCommandFromResourceAssembler;
import com.pezbackend.tenancy.interfaces.rest.transform.RestaurantResourceFromEntityAssembler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing restaurant management and onboarding endpoints.
 */
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final RestaurantCommandService restaurantCommandService;
    private final RestaurantRepository restaurantRepository;

    public RestaurantController(
            RestaurantCommandService restaurantCommandService,
            RestaurantRepository restaurantRepository
    ) {
        this.restaurantCommandService = restaurantCommandService;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Endpoint público para registrar un nuevo restaurante junto a su usuario administrador de forma transaccional.
     *
     * @param resource datos de registro
     * @return recurso de restaurante creado
     */
    @PostMapping("/onboarding")
    public ResponseEntity<RestaurantResource> onboarding(@Valid @RequestBody OnboardingResource resource) {
        OnboardingCommand command = OnboardingCommandFromResourceAssembler.toCommandFromResource(resource);
        Restaurant restaurant = restaurantCommandService.handleOnboarding(command);
        RestaurantResource response = RestaurantResourceFromEntityAssembler.toResourceFromEntity(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene los datos del restaurante por su identificador. Requiere pertenecer al mismo tenant.
     *
     * @param id identificador del restaurante
     * @return recurso de restaurante
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResource> getRestaurantById(@PathVariable Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null || !currentTenantId.equals(id)) {
            // Retorna 404 para ocultar la existencia de otros inquilinos
            throw new TenantMismatchException("Restaurant", id);
        }

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESTAURANT_NOT_FOUND", "Restaurante no encontrado con ID: " + id));
        RestaurantResource response = RestaurantResourceFromEntityAssembler.toResourceFromEntity(restaurant);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza la información del restaurante actual. Requiere pertenecer al mismo tenant.
     *
     * @param id       identificador del restaurante
     * @param resource datos actualizados
     * @return recurso de restaurante actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResource> updateRestaurant(@PathVariable Long id, @Valid @RequestBody OnboardingResource resource) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null || !currentTenantId.equals(id)) {
            throw new TenantMismatchException("Restaurant", id);
        }

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESTAURANT_NOT_FOUND", "Restaurante no encontrado con ID: " + id));

        restaurant.setName(resource.name());
        restaurant.setBusinessDocumentNumber(resource.businessDocumentNumber());
        restaurant.setContactEmail(resource.contactEmail());
        restaurant.setContactPhone(resource.contactPhone());
        restaurant = restaurantRepository.save(restaurant);

        RestaurantResource response = RestaurantResourceFromEntityAssembler.toResourceFromEntity(restaurant);
        return ResponseEntity.ok(response);
    }
}
