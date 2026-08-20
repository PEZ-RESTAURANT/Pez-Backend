package com.pezbackend.kitchen.application.internal.services;

import com.pezbackend.kitchen.domain.model.entities.PrintStation;
import com.pezbackend.kitchen.domain.services.PrintStationQueryService;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.PrintStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrintStationQueryServiceImpl implements PrintStationQueryService {

    private final PrintStationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PrintStation> getAllPrintStations() {
        return repository.findAll();
    }
}
