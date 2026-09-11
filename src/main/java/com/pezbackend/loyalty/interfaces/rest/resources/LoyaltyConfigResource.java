package com.pezbackend.loyalty.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso DTO para la configuración del programa de fidelización.
 */
public record LoyaltyConfigResource(
        BigDecimal minPurchaseAmountForPoints,
        BigDecimal pointsPerCurrencyUnit,
        int reviewSatisfactionThreshold,
        String googleReviewUrl,
        String qrCodeImage
) {}
