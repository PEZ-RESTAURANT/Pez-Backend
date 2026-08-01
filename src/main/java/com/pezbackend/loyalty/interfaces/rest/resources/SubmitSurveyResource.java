package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO para enviar una encuesta de satisfacción.
 */
public record SubmitSurveyResource(
        String favoriteDish,
        String favoriteDrink,
        int serviceSatisfaction,
        int foodSatisfaction,
        String suggestion,
        String date
) {}
