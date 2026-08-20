package com.pezbackend.inventory.application.internal.services;

import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.model.entities.StockLevel;
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
    public Supply createSupply(String name, String unit, BigDecimal minThreshold, BigDecimal criticalThreshold) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_SUPPLY_NAME", "El nombre del insumo no puede estar vacío.");
        }
        if (supplyRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleViolationException("SUPPLY_ALREADY_EXISTS", "Ya existe un insumo registrado con el nombre: " + name);
        }
        if (minThreshold != null && minThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_MIN_THRESHOLD", "El umbral mínimo no puede ser negativo.");
        }
        if (criticalThreshold != null && criticalThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_CRITICAL_THRESHOLD", "El umbral crítico no puede ser negativo.");
        }

        Supply supply = new Supply(name.trim(), unit != null ? unit.trim() : null, minThreshold, criticalThreshold);
        return supplyRepository.save(supply);
    }

    @Override
    public Supply updateSupply(Long id, String name, String unit, BigDecimal minThreshold, BigDecimal criticalThreshold) {
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
        if (criticalThreshold != null && criticalThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_CRITICAL_THRESHOLD", "El umbral crítico no puede ser negativo.");
        }

        supply.setName(name.trim());
        supply.setUnit(unit != null ? unit.trim() : null);
        supply.setMinThreshold(minThreshold);
        supply.setCriticalThreshold(criticalThreshold);

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

        StockLevel oldLevel = supply.getStockLevelFor(oldStock);
        StockLevel newLevel = supply.getStockLevelFor(newStock);
        if (oldLevel != newLevel && (newLevel == StockLevel.AGOTADO || newLevel == StockLevel.CRITICO || newLevel == StockLevel.BAJO)) {
            if (newLevel.ordinal() < oldLevel.ordinal()) {
                eventPublisher.publishEvent(new LowStockAlertTriggered(id, supply.getName(), newStock, supply.getMinThreshold(), newLevel));
            }
        }
    }

    @Override
    public void adjustManual(Long id, BigDecimal targetStock, String reason, String registeredBy) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SUPPLY_NOT_FOUND", "Insumo no encontrado con ID: " + id));

        if (targetStock == null || targetStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("INVALID_QUANTITY", "La cantidad física actual no puede ser menor que cero.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("REASON_REQUIRED", "Debe proporcionar un motivo para realizar el ajuste manual.");
        }

        BigDecimal oldStock = supply.getCurrentStock();
        BigDecimal delta = targetStock.subtract(oldStock);

        supply.setCurrentStock(targetStock);
        supplyRepository.save(supply);

        // Format detailed reason containing previous value, new value and net difference
        String formattedReason = String.format("Ajuste manual: %s → %s (%s%s) | Motivo: %s",
                oldStock.stripTrailingZeros().toPlainString(),
                targetStock.stripTrailingZeros().toPlainString(),
                delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "",
                delta.stripTrailingZeros().toPlainString(),
                reason
        );

        StockMovement movement = new StockMovement(id, StockMovementType.MANUAL_ADJUSTMENT, delta, registeredBy, formattedReason);
        stockMovementRepository.save(movement);

        // Publicar eventos de dominio correspondientes con la diferencia calculada
        eventPublisher.publishEvent(new StockAdjustedManually(id, delta, targetStock, registeredBy, reason));

        StockLevel oldLevel = supply.getStockLevelFor(oldStock);
        StockLevel newLevel = supply.getStockLevelFor(targetStock);
        if (oldLevel != newLevel && (newLevel == StockLevel.AGOTADO || newLevel == StockLevel.CRITICO || newLevel == StockLevel.BAJO)) {
            if (newLevel.ordinal() < oldLevel.ordinal()) {
                eventPublisher.publishEvent(new LowStockAlertTriggered(id, supply.getName(), targetStock, supply.getMinThreshold(), newLevel));
            }
        }

        // Alerta de descuadre si queda en negativo
        if (targetStock.compareTo(BigDecimal.ZERO) < 0) {
            eventPublisher.publishEvent(new StockMismatchDetected(id, supply.getName(), delta.abs(), oldStock));
        }
    }
}
