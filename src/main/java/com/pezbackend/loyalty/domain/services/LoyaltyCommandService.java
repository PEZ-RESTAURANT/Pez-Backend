package com.pezbackend.loyalty.domain.services;

import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.domain.model.entities.SatisfactionSurvey;
import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Servicio de dominio/aplicación para las operaciones de comando (escritura) del programa de fidelización.
 */
public interface LoyaltyCommandService {

    Customer registerCustomer(String phone, String fullName, String email, LocalDate birthday, String address, boolean dataConsentAccepted);

    void deleteCustomer(Long id);

    SatisfactionSurvey submitSurvey(Long customerId, String favoriteDish, String favoriteDrink, int serviceSatisfaction, int foodSatisfaction, String suggestion, LocalDate date);

    PointsTransaction redeemPoints(Long customerId, int points, LocalDate date);

    LoyaltyConfig updateConfig(BigDecimal minPurchaseAmountForPoints, BigDecimal pointsPerCurrencyUnit, int reviewSatisfactionThreshold, String googleReviewUrl, String qrCodeImage);

    void sendManualPromotion(Long customerId, String subject, String message);
}
