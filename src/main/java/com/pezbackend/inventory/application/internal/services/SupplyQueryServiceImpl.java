package com.pezbackend.inventory.application.internal.services;

import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.services.SupplyQueryService;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.StockMovementRepository;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de consultas para insumos y stock.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplyQueryServiceImpl implements SupplyQueryService {

    private final SupplyRepository supplyRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public List<Supply> getAllSupplies() {
        return supplyRepository.findAll();
    }

    @Override
    public Optional<Supply> getSupplyById(Long id) {
        return supplyRepository.findById(id);
    }

    @Override
    public List<StockMovement> getMovementsForSupply(Long supplyId) {
        if (!supplyRepository.existsById(supplyId)) {
            throw new ResourceNotFoundException("SUPPLY_NOT_FOUND", "Insumo no encontrado con ID: " + supplyId);
        }
        return stockMovementRepository.findAllBySupplyIdOrderByDateDesc(supplyId);
    }

    @Override
    public List<Supply> getLowStockSupplies() {
        return supplyRepository.findAll().stream()
                .filter(s -> s.getMinThreshold() != null && s.getCurrentStock().compareTo(s.getMinThreshold()) < 0)
                .toList();
    }

    @Override
    public List<Supply> getMismatchedSupplies() {
        return supplyRepository.findAll().stream()
                .filter(s -> s.getCurrentStock().compareTo(BigDecimal.ZERO) < 0)
                .toList();
    }
}
