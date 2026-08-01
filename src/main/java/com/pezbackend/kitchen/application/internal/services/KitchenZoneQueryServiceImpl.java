package com.pezbackend.kitchen.application.internal.services;

import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.KitchenZoneRepository;
import com.pezbackend.kitchen.domain.services.KitchenZoneQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de consultas para zonas de cocina.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KitchenZoneQueryServiceImpl implements KitchenZoneQueryService {

    private final KitchenZoneRepository repository;

    @Override
    public List<KitchenZone> getAllZones() {
        return repository.findAll();
    }

    @Override
    public Optional<KitchenZone> getZoneById(Long id) {
        return repository.findById(id);
    }
}
