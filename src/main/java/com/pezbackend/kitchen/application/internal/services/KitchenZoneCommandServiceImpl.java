package com.pezbackend.kitchen.application.internal.services;

import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.KitchenZoneRepository;
import com.pezbackend.kitchen.domain.services.KitchenZoneCommandService;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de comandos para gestionar zonas de cocina.
 */
@Service
@RequiredArgsConstructor
public class KitchenZoneCommandServiceImpl implements KitchenZoneCommandService {

    private final KitchenZoneRepository repository;

    @Override
    @Transactional
    public KitchenZone createZone(String name, boolean printingEnabled) {
        if (name == null || name.strip().isEmpty()) {
            throw new BusinessRuleViolationException("INVALID_NAME", "El nombre de la zona de cocina no puede estar vacío.");
        }
        if (repository.existsByName(name)) {
            throw new BusinessRuleViolationException("ZONE_EXISTS", "Ya existe una zona de cocina con el nombre: " + name);
        }

        KitchenZone zone = new KitchenZone(name);
        zone.setPrintingEnabled(printingEnabled);
        return repository.save(zone);
    }

    @Override
    @Transactional
    public KitchenZone updateZone(Long id, String name, boolean printingEnabled) {
        if (name == null || name.strip().isEmpty()) {
            throw new BusinessRuleViolationException("INVALID_NAME", "El nombre de la zona de cocina no puede estar vacío.");
        }

        KitchenZone zone = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KITCHEN_ZONE_NOT_FOUND", "Zona de cocina no encontrada con ID: " + id));

        repository.findByName(name).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new BusinessRuleViolationException("ZONE_EXISTS", "Ya existe otra zona de cocina con el nombre: " + name);
            }
        });

        zone.setName(name);
        zone.setPrintingEnabled(printingEnabled);
        return repository.save(zone);
    }

    @Override
    @Transactional
    public void deleteZone(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("KITCHEN_ZONE_NOT_FOUND", "Zona de cocina no encontrada con ID: " + id);
        }
        repository.deleteById(id);
    }
}
