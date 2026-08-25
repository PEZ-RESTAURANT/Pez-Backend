package com.pezbackend.loyalty.application.internal.commandservices;

import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.domain.model.entities.SatisfactionSurvey;
import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;
import com.pezbackend.loyalty.domain.model.valueobjects.PointsTransactionType;
import com.pezbackend.loyalty.domain.model.events.*;
import com.pezbackend.loyalty.domain.services.LoyaltyCommandService;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.*;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementación de LoyaltyCommandService encargada de gestionar los comandos de fidelización.
 */
@Service
@RequiredArgsConstructor
public class LoyaltyCommandServiceImpl implements LoyaltyCommandService {

    private final CustomerRepository customerRepository;
    private final SatisfactionSurveyRepository satisfactionSurveyRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final LoyaltyConfigRepository loyaltyConfigRepository;
    private final MarketingNotificationLogRepository marketingNotificationLogRepository;
    private final com.pezbackend.shared.infrastructure.notification.EmailNotificationChannel emailNotificationChannel;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Customer registerCustomer(String phone, String fullName, String email, LocalDate birthday, String address, boolean dataConsentAccepted) {
        if (!dataConsentAccepted) {
            throw new BusinessRuleViolationException("DATA_CONSENT_REQUIRED", "El consentimiento de datos es obligatorio para afiliarse al programa de fidelización.");
        }
        java.util.Optional<Customer> existingOpt = customerRepository.findByPhone(phone);
        if (existingOpt.isPresent()) {
            Customer existing = existingOpt.get();
            if (existing.isAffiliated()) {
                throw new BusinessRuleViolationException("CUSTOMER_ALREADY_EXISTS", "Un cliente con el número de teléfono " + phone + " ya está afiliado.");
            }
            existing.setFullName(fullName);
            if (email != null) existing.setEmail(email);
            if (birthday != null) existing.setBirthday(birthday);
            if (address != null) existing.setAddress(address);
            existing.setAffiliated(dataConsentAccepted);
            if (dataConsentAccepted) {
                existing.setDataConsentDate(java.time.LocalDateTime.now());
            }
            return customerRepository.save(existing);
        }

        Customer customer = new Customer(phone, fullName, email, birthday, address, dataConsentAccepted);
        customer = customerRepository.save(customer);

        eventPublisher.publishEvent(new CustomerRegistered(customer.getId(), customer.getPhone(), customer.getFullName()));
        return customer;
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Cliente no encontrado."));

        // Eliminar encuestas asociadas
        List<SatisfactionSurvey> surveys = satisfactionSurveyRepository.findAllByCustomerId(id);
        satisfactionSurveyRepository.deleteAll(surveys);

        // Eliminar transacciones de puntos asociadas
        List<PointsTransaction> transactions = pointsTransactionRepository.findAllByCustomerId(id);
        pointsTransactionRepository.deleteAll(transactions);

        customerRepository.delete(customer);

        eventPublisher.publishEvent(new CustomerDeleted(id));
    }

    @Override
    @Transactional
    public SatisfactionSurvey submitSurvey(Long customerId, String favoriteDish, String favoriteDrink, int serviceSatisfaction, int foodSatisfaction, String suggestion, LocalDate date) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Cliente no encontrado.");
        }

        SatisfactionSurvey survey = new SatisfactionSurvey(
                customerId, favoriteDish, favoriteDrink, serviceSatisfaction, foodSatisfaction, suggestion, date
        );
        survey = satisfactionSurveyRepository.save(survey);

        eventPublisher.publishEvent(new SurveySubmitted(survey.getId(), customerId, serviceSatisfaction, foodSatisfaction));
        return survey;
    }

    @Override
    @Transactional
    public PointsTransaction redeemPoints(Long customerId, int points, LocalDate date) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Cliente no encontrado."));

        customer.redeemPoints(points);
        customerRepository.save(customer);

        PointsTransaction transaction = new PointsTransaction(customerId, PointsTransactionType.REDEEMED, points, null, date);
        transaction = pointsTransactionRepository.save(transaction);

        eventPublisher.publishEvent(new PointsRedeemed(customerId, points));
        return transaction;
    }

    @Override
    @Transactional
    public LoyaltyConfig updateConfig(BigDecimal minPurchaseAmountForPoints, BigDecimal pointsPerCurrencyUnit, int reviewSatisfactionThreshold, String googleReviewUrl) {
        if (minPurchaseAmountForPoints == null || minPurchaseAmountForPoints.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_CONFIG", "El monto mínimo de compra no puede ser menor a cero.");
        }
        if (pointsPerCurrencyUnit == null || pointsPerCurrencyUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_CONFIG", "Los puntos por unidad monetaria no pueden ser menores a cero.");
        }
        if (reviewSatisfactionThreshold < 1 || reviewSatisfactionThreshold > 5) {
            throw new BusinessRuleViolationException("INVALID_CONFIG", "El umbral de satisfacción debe estar entre 1 y 5.");
        }

        List<LoyaltyConfig> configs = loyaltyConfigRepository.findAll();
        LoyaltyConfig config;
        if (configs.isEmpty()) {
            config = new LoyaltyConfig(minPurchaseAmountForPoints, pointsPerCurrencyUnit, reviewSatisfactionThreshold, googleReviewUrl);
        } else {
            config = configs.get(0);
            config.setMinPurchaseAmountForPoints(minPurchaseAmountForPoints);
            config.setPointsPerCurrencyUnit(pointsPerCurrencyUnit);
            config.setReviewSatisfactionThreshold(reviewSatisfactionThreshold);
            config.setGoogleReviewUrl(googleReviewUrl);
        }
        return loyaltyConfigRepository.save(config);
    }

    @Override
    @Transactional
    public void sendManualPromotion(Long customerId, String subject, String message) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "Cliente no encontrado con ID: " + customerId));

        if (!customer.isDataConsentAccepted()) {
            throw new BusinessRuleViolationException("DATA_CONSENT_REQUIRED", "El cliente no ha aceptado el consentimiento de tratamiento de datos.");
        }
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new BusinessRuleViolationException("CUSTOMER_EMAIL_MISSING", "El cliente no tiene un correo electrónico registrado.");
        }

        // Validar límite: Máximo 2 promociones por mes calendario
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<com.pezbackend.loyalty.domain.model.entities.MarketingNotificationLog> logs = 
                marketingNotificationLogRepository.findAllByCustomerIdAndSentAtAfter(customerId, startOfMonth);
        if (logs.size() >= 2) {
            throw new BusinessRuleViolationException("PROMOTION_LIMIT_EXCEEDED", "Se ha superado el límite de 2 promociones en este mes para el cliente.");
        }

        // Despachar email
        Map<String, Object> model = Map.of(
            "title", "Promoción Especial",
            "subtitle", "Al Toque - Club de Fidelización",
            "greeting", "Hola, " + customer.getFullName() + ":",
            "paragraphs", List.of(message),
            "isSuccess", true
        );
        emailNotificationChannel.send(customer.getEmail(), subject, "email-template", model);

        // Registrar log
        com.pezbackend.loyalty.domain.model.entities.MarketingNotificationLog log = 
                new com.pezbackend.loyalty.domain.model.entities.MarketingNotificationLog(customerId, "PROMO");
        marketingNotificationLogRepository.save(log);
    }
}
