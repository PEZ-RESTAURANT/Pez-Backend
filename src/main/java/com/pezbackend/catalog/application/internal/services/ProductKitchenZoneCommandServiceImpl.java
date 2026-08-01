package com.pezbackend.catalog.application.internal.services;

import com.pezbackend.catalog.domain.model.entities.ProductKitchenZone;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductKitchenZoneRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.domain.services.ProductKitchenZoneCommandService;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.KitchenZoneRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de comandos para gestionar la asignación de productos a zonas de cocina.
 */
@Service
@RequiredArgsConstructor
public class ProductKitchenZoneCommandServiceImpl implements ProductKitchenZoneCommandService {

    private final ProductRepository productRepository;
    private final ProductKitchenZoneRepository productKitchenZoneRepository;
    private final KitchenZoneRepository kitchenZoneRepository;

    @Override
    @Transactional
    public void assignProductToZone(Long productId, Long zoneId) {
        // 1. Validar que el producto exista
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Producto no encontrado con ID: " + productId);
        }

        // 2. Validar que la zona de cocina exista
        if (!kitchenZoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("KITCHEN_ZONE_NOT_FOUND", "Zona de cocina no encontrada con ID: " + zoneId);
        }

        // 3. Crear o actualizar la asignación
        ProductKitchenZone assignment = productKitchenZoneRepository.findByProductId(productId)
                .orElseGet(() -> new ProductKitchenZone(productId, zoneId));
        
        assignment.setZoneId(zoneId);
        productKitchenZoneRepository.save(assignment);
    }
}
