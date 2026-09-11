package com.pezbackend.loyalty.interfaces.rest;

import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.domain.model.entities.SatisfactionSurvey;
import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;
import com.pezbackend.loyalty.domain.services.LoyaltyCommandService;
import com.pezbackend.loyalty.domain.services.LoyaltyQueryService;
import com.pezbackend.loyalty.interfaces.rest.resources.*;
import com.pezbackend.loyalty.interfaces.rest.transform.*;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST que expone los endpoints para la gestión de clientes, encuestas de satisfacción,
 * canje de puntos y configuración del programa de fidelización.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyCommandService commandService;
    private final LoyaltyQueryService queryService;

    @PostMapping("/customers")
    @RequiresPermission("loyalty.register_customer")
    public ResponseEntity<CustomerResource> registerCustomer(@RequestBody CreateCustomerResource resource) {
        LocalDate birthday = (resource.birthday() == null || resource.birthday().isBlank())
                ? null : LocalDate.parse(resource.birthday());

        Customer customer = commandService.registerCustomer(
                resource.phone(),
                resource.fullName(),
                resource.email(),
                birthday,
                resource.address(),
                resource.dataConsentAccepted()
        );
        return ResponseEntity.ok(CustomerResourceAssembler.toResource(customer));
    }

    @GetMapping("/customers/by-phone/{phone}")
    @RequiresPermission("loyalty.register_customer")
    public ResponseEntity<CustomerResource> getCustomerByPhone(@PathVariable String phone) {
        Customer customer = queryService.getCustomerByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Cliente con teléfono " + phone + " no encontrado."));
        return ResponseEntity.ok(CustomerResourceAssembler.toResource(customer));
    }

    @GetMapping("/customers/{id}")
    @RequiresPermission("loyalty.view")
    public ResponseEntity<CustomerResource> getCustomerById(@PathVariable Long id) {
        Customer customer = queryService.getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Cliente no encontrado."));
        return ResponseEntity.ok(CustomerResourceAssembler.toResource(customer));
    }

    @DeleteMapping("/customers/{id}")
    @RequiresPermission("loyalty.register_customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        commandService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/customers/{id}/survey")
    @RequiresPermission("loyalty.register_customer")
    public ResponseEntity<SurveyResponseResource> submitSurvey(@PathVariable Long id, @RequestBody SubmitSurveyResource resource) {
        LocalDate date = (resource.date() == null || resource.date().isBlank())
                ? LocalDate.now() : LocalDate.parse(resource.date());

        // Guardar la encuesta
        commandService.submitSurvey(
                id,
                resource.favoriteDish(),
                resource.favoriteDrink(),
                resource.serviceSatisfaction(),
                resource.foodSatisfaction(),
                resource.suggestion(),
                date
        );

        // Evaluar redirección a reseña de Google
        LoyaltyConfig config = queryService.getConfig();
        double averageScore = (resource.serviceSatisfaction() + resource.foodSatisfaction()) / 2.0;
        boolean showReviewPrompt = averageScore >= config.getReviewSatisfactionThreshold();
        String reviewUrl = showReviewPrompt ? config.getGoogleReviewUrl() : null;

        return ResponseEntity.ok(new SurveyResponseResource(showReviewPrompt, reviewUrl));
    }

    @PostMapping("/customers/{id}/redeem-points")
    @RequiresPermission("loyalty.redeem_points")
    public ResponseEntity<PointsTransactionResource> redeemPoints(@PathVariable Long id, @RequestBody RedeemPointsResource resource) {
        LocalDate date = (resource.date() == null || resource.date().isBlank())
                ? LocalDate.now() : LocalDate.parse(resource.date());

        PointsTransaction transaction = commandService.redeemPoints(id, resource.points(), date);
        return ResponseEntity.ok(PointsTransactionResourceAssembler.toResource(transaction));
    }

    @GetMapping("/customers/{id}/points-history")
    @RequiresPermission("loyalty.view")
    public ResponseEntity<List<PointsTransactionResource>> getPointsHistory(@PathVariable Long id) {
        List<PointsTransactionResource> resources = queryService.getPointsHistory(id).stream()
                .map(PointsTransactionResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/loyalty/config")
    @RequiresPermission("loyalty.manage_config")
    public ResponseEntity<LoyaltyConfigResource> getConfig() {
        LoyaltyConfig config = queryService.getConfig();
        return ResponseEntity.ok(LoyaltyConfigResourceAssembler.toResource(config));
    }

    @PutMapping("/loyalty/config")
    @RequiresPermission("loyalty.manage_config")
    public ResponseEntity<LoyaltyConfigResource> updateConfig(@RequestBody LoyaltyConfigResource resource) {
        LoyaltyConfig config = commandService.updateConfig(
                resource.minPurchaseAmountForPoints(),
                resource.pointsPerCurrencyUnit(),
                resource.reviewSatisfactionThreshold(),
                resource.googleReviewUrl(),
                resource.qrCodeImage()
        );
        return ResponseEntity.ok(LoyaltyConfigResourceAssembler.toResource(config));
    }

    @PostMapping("/customers/{id}/send-promotion")
    @RequiresPermission("loyalty.register_customer")
    public ResponseEntity<Void> sendPromotion(@PathVariable Long id, @RequestBody SendPromotionResource resource) {
        commandService.sendManualPromotion(id, resource.subject(), resource.message());
        return ResponseEntity.ok().build();
    }
}
