package com.pezbackend.inventory.application.internal.services;

import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.model.events.LowStockAlertTriggered;
import com.pezbackend.inventory.domain.model.events.StockAdjustedManually;
import com.pezbackend.inventory.domain.model.events.StockMismatchDetected;
import com.pezbackend.inventory.domain.model.valueobjects.StockMovementType;
import com.pezbackend.inventory.domain.services.SupplyCommandService;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.StockMovementRepository;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del servicio de comandos para insumos e inventario.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SupplyCommandServiceImpl implements SupplyCommandService {

    private final SupplyRepository supplyRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Supply createSupply(String name, String unit, BigDecimal minThreshold) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_SUPPLY_NAME", "El nombre del insumo no puede estar vacío.");
        }
        if (supplyRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleViolationException("SUPPLY_ALREADY_EXISTS", "Ya existe un insumo registrado con el nombre: " + name);
        }
        if (minThreshold != null && minThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_MIN_THRESHOLD", "El umbral mínimo no puede ser negativo.");
        }

        Supply supply = new Supply(name.trim(), unit != null ? unit.trim() : null, minThreshold);
        return supplyRepository.save(supply);
    }

    @Override
    public Supply updateSupply(Long id, String name, String unit, BigDecimal minThreshold) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SUPPLY_NOT_FOUND", "Insumo no encontrado con ID: " + id));

        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_SUPPLY_NAME", "El nombre del insumo no puede estar vacío.");
        }

        supplyRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessRuleViolationException("SUPPLY_ALREADY_EXISTS", "Ya existe otro insumo registrado con el nombre: " + name);
            }
        });

        if (minThreshold != null && minThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_MIN_THRESHOLD", "El umbral mínimo no puede ser negativo.");
        }

        supply.setName(name.trim());
        supply.setUnit(unit != null ? unit.trim() : null);
        supply.setMinThreshold(minThreshold);

        return supplyRepository.save(supply);
    }

    @Override
    public void deleteSupply(Long id) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SUPPLY_NOT_FOUND", "Insumo no encontrado con ID: " + id));
        supplyRepository.delete(supply);
    }

    @Override
    public void restock(Long id, BigDecimal quantity, String registeredBy) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SUPPLY_NOT_FOUND", "Insumo no encontrado con ID: " + id));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("INVALID_QUANTITY", "La cantidad para reabastecimiento debe ser mayor a cero.");
        }

        BigDecimal oldStock = supply.getCurrentStock();
        BigDecimal newStock = oldStock.add(quantity);
        supply.setCurrentStock(newStock);
        supplyRepository.save(supply);

        StockMovement movement = new StockMovement(id, StockMovementType.RESTOCK, quantity, registeredBy, "Reabastecimiento de insumo");
        stockMovementRepository.save(movement);
    }

    @Override
    public void adjustManual(Long id, BigDecimal quantity, String reason, String registeredBy) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SUPPLY_NOT_FOUND", "Insumo no encontrado con ID: " + id));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessRuleViolationException("INVALID_QUANTITY", "La cantidad de ajuste no puede ser cero.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("REASON_REQUIRED", "Debe proporcionar un motivo para realizar el ajuste manual.");
        }

        BigDecimal oldStock = supply.getCurrentStock();
        BigDecimal newStock = oldStock.add(quantity);
        supply.setCurrentStock(newStock);
        supplyRepository.save(supply);

        StockMovement movement = new StockMovement(id, StockMovementType.MANUAL_ADJUSTMENT, quantity, registeredBy, reason);
        stockMovementRepository.save(movement);

        // Publicar eventos de dominio correspondientes
        eventPublisher.publishEvent(new StockAdjustedManually(id, quantity, newStock, registeredBy, reason));

        // Alerta de stock bajo si cruza el umbral
        if (supply.getMinThreshold() != null && newStock.compareTo(supply.getMinThreshold()) < 0 && oldStock.compareTo(supply.getMinThreshold()) >= 0) {
            eventPublisher.publishEvent(new LowStockAlertTriggered(id, supply.getName(), newStock, supply.getMinThreshold()));
        }

        // Alerta de descuadre si queda en negativo
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            eventPublisher.publishEvent(new StockMismatchDetected(id, supply.getName(), quantity.abs(), oldStock));
        }
    }
}
