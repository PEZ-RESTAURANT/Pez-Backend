package com.pezbackend.catalog.application.internal.services;

import com.pezbackend.catalog.domain.model.entities.ProductKitchenZone;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductKitchenZoneRepository;
import com.pezbackend.catalog.domain.services.ProductKitchenZoneQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de consultas para obtener información sobre la asignación de productos a zonas de cocina.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductKitchenZoneQueryServiceImpl implements ProductKitchenZoneQueryService {

    private final ProductKitchenZoneRepository repository;

    @Override
    public Optional<Long> getZoneIdForProduct(Long productId) {
        return repository.findByProductId(productId).map(ProductKitchenZone::getZoneId);
    }

    @Override
    public Set<Long> getProductIdsForZone(Long zoneId) {
        return repository.findAllByZoneId(zoneId).stream()
                .map(ProductKitchenZone::getProductId)
                .collect(Collectors.toSet());
    }
}
