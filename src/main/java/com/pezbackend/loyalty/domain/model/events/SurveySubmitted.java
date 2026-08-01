package com.pezbackend.loyalty.domain.model.events;

import com.pezbackend.shared.domain.model.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento emitido al enviar una encuesta de satisfacción.
 */
public record SurveySubmitted(
        Long surveyId,
        Long customerId,
        int serviceSatisfaction,
        int foodSatisfaction,
        LocalDateTime timestamp
) implements DomainEvent {

    public SurveySubmitted(Long surveyId, Long customerId, int serviceSatisfaction, int foodSatisfaction) {
        this(surveyId, customerId, serviceSatisfaction, foodSatisfaction, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "SurveySubmitted";
    }

    @Override
    public String module() {
        return "loyalty";
    }

    @Override
    public String userId() {
        return "system";
    }

    @Override
    public Object payload() {
        return Map.of(
                "surveyId", surveyId,
                "customerId", customerId,
                "serviceSatisfaction", serviceSatisfaction,
                "foodSatisfaction", foodSatisfaction
        );
    }
}
