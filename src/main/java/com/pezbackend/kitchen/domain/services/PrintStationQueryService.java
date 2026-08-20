package com.pezbackend.kitchen.domain.services;

import com.pezbackend.kitchen.domain.model.entities.PrintStation;
import java.util.List;

public interface PrintStationQueryService {
    List<PrintStation> getAllPrintStations();
}
