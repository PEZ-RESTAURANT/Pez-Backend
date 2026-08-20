package com.pezbackend.kitchen.domain.services;

import com.pezbackend.kitchen.domain.model.entities.PrintStation;

public interface PrintStationCommandService {
    PrintStation createPrintStation(String name);
    void deletePrintStation(Long id);
}
