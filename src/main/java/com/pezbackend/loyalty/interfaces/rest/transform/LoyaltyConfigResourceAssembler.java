package com.pezbackend.loyalty.interfaces.rest.transform;

import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;
import com.pezbackend.loyalty.interfaces.rest.resources.LoyaltyConfigResource;

/**
 * Ensamblador para convertir la entidad LoyaltyConfig a su DTO LoyaltyConfigResource.
 */
public class LoyaltyConfigResourceAssembler {

    public static LoyaltyConfigResource toResource(LoyaltyConfig config) {
        return new LoyaltyConfigResource(
                config.getMinPurchaseAmountForPoints(),
                config.getPointsPerCurrencyUnit(),
                config.getReviewSatisfactionThreshold(),
                config.getGoogleReviewUrl()
        );
    }
}
