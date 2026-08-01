package com.pezbackend.loyalty.interfaces.rest.resources;

/**
 * Recurso DTO de respuesta para la encuesta de satisfacción.
 */
public record SurveyResponseResource(
        boolean showReviewPrompt,
        String reviewUrl
) {}
