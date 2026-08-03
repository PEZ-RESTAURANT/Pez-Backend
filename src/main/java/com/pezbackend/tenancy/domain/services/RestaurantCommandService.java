package com.pezbackend.tenancy.domain.services;

import com.pezbackend.tenancy.domain.model.aggregates.Restaurant;
import com.pezbackend.tenancy.domain.model.commands.OnboardingCommand;

/**
 * Servicio de comandos para realizar operaciones en el agregado {@link Restaurant}.
 */
public interface RestaurantCommandService {

    /**
     * Realiza el registro de un nuevo restaurante y su administrador inicial de forma transaccional.
     *
     * @param command datos del restaurante y administrador inicial
     * @return restaurante creado
     */
    Restaurant handleOnboarding(OnboardingCommand command);
}
