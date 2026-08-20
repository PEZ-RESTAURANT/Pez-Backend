package com.pezbackend.kitchen.interfaces.rest;

import com.pezbackend.kitchen.domain.model.entities.PrintStation;
import com.pezbackend.kitchen.domain.services.PrintStationCommandService;
import com.pezbackend.kitchen.domain.services.PrintStationQueryService;
import com.pezbackend.kitchen.interfaces.rest.resources.CreatePrintStationResource;
import com.pezbackend.kitchen.interfaces.rest.resources.PrintStationResource;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen/print-stations")
@RequiredArgsConstructor
public class PrintStationsController {

    private final PrintStationCommandService commandService;
    private final PrintStationQueryService queryService;

    @GetMapping
    public ResponseEntity<List<PrintStationResource>> getAllPrintStations() {
        List<PrintStation> stations = queryService.getAllPrintStations();
        return ResponseEntity.ok(
                stations.stream()
                        .map(s -> new PrintStationResource(s.getId(), s.getName()))
                        .toList()
        );
    }

    @PostMapping
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<PrintStationResource> createPrintStation(@RequestBody CreatePrintStationResource resource) {
        PrintStation station = commandService.createPrintStation(resource.name());
        return ResponseEntity.ok(new PrintStationResource(station.getId(), station.getName()));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<Void> deletePrintStation(@PathVariable Long id) {
        commandService.deletePrintStation(id);
        return ResponseEntity.ok().build();
    }
}
