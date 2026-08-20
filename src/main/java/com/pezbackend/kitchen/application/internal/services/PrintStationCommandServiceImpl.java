package com.pezbackend.kitchen.application.internal.services;

import com.pezbackend.kitchen.domain.model.entities.PrintStation;
import com.pezbackend.kitchen.domain.services.PrintStationCommandService;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.PrintStationRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrintStationCommandServiceImpl implements PrintStationCommandService {

    private final PrintStationRepository repository;

    @Override
    @Transactional
    public PrintStation createPrintStation(String name) {
        if (name == null || name.strip().isEmpty()) {
            throw new BusinessRuleViolationException("INVALID_NAME", "El nombre del puesto de impresión no puede estar vacío.");
        }
        if (repository.existsByName(name)) {
            throw new BusinessRuleViolationException("PRINT_STATION_EXISTS", "Ya existe una estación de impresión con el nombre: " + name);
        }

        PrintStation station = new PrintStation(name);
        return repository.save(station);
    }

    @Override
    @Transactional
    public void deletePrintStation(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("PRINT_STATION_NOT_FOUND", "Estación de impresión no encontrada con ID: " + id);
        }
        repository.deleteById(id);
    }
}
