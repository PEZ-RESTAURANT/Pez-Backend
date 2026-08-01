package com.pezbackend.loyalty.domain.services;

import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.domain.model.entities.PointsTransaction;
import com.pezbackend.loyalty.domain.model.entities.LoyaltyConfig;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio/aplicación para las operaciones de consulta (lectura) del programa de fidelización.
 */
public interface LoyaltyQueryService {

    Optional<Customer> getCustomerById(Long id);

    Optional<Customer> getCustomerByPhone(String phone);

    List<PointsTransaction> getPointsHistory(Long customerId);

    LoyaltyConfig getConfig();
}
