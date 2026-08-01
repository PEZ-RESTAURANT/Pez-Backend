package com.pezbackend.analytics.application.internal.commandservices;

import com.pezbackend.analytics.domain.model.entities.AnalyticsConfig;
import com.pezbackend.analytics.domain.services.AnalyticsCommandService;
import com.pezbackend.analytics.infrastructure.persistence.jpa.repositories.AnalyticsConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de AnalyticsCommandService encargada de gestionar los comandos de configuración de analítica.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsCommandServiceImpl implements AnalyticsCommandService {

    private final AnalyticsConfigRepository repository;

    @Override
    @Transactional
    public AnalyticsConfig updateConfig(Integer lowSalesThresholdUnits, Integer lowSalesEvaluationPeriodDays, String datePresets) {
        List<AnalyticsConfig> configs = repository.findAll();
        AnalyticsConfig config;
        if (configs.isEmpty()) {
            config = new AnalyticsConfig(lowSalesThresholdUnits, lowSalesEvaluationPeriodDays, datePresets);
        } else {
            config = configs.get(0);
            config.setLowSalesThresholdUnits(lowSalesThresholdUnits);
            config.setLowSalesEvaluationPeriodDays(lowSalesEvaluationPeriodDays);
            if (datePresets != null) {
                config.setDatePresets(datePresets);
            }
        }
        return repository.save(config);
    }
}
