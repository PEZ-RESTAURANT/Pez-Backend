package com.pezbackend.kitchen.interfaces.rest;

import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.domain.services.KitchenZoneCommandService;
import com.pezbackend.kitchen.domain.services.KitchenZoneQueryService;
import com.pezbackend.kitchen.interfaces.rest.resources.CreateKitchenZoneResource;
import com.pezbackend.kitchen.interfaces.rest.resources.KitchenZoneResource;
import com.pezbackend.kitchen.interfaces.rest.transform.KitchenZoneResourceFromEntityAssembler;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión del catálogo de zonas de cocina.
 */
@RestController
@RequestMapping("/api/v1/kitchen/zones")
@RequiredArgsConstructor
public class KitchenZonesController {

    private final KitchenZoneCommandService commandService;
    private final KitchenZoneQueryService queryService;

    /**
     * Obtiene el listado completo de zonas de cocina.
     *
     * @return lista de recursos de zona de cocina
     */
    @GetMapping
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<List<KitchenZoneResource>> getAllZones() {
        List<KitchenZone> zones = queryService.getAllZones();
        return ResponseEntity.ok(
                zones.stream()
                        .map(KitchenZoneResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    /**
     * Crea una nueva zona de cocina.
     *
     * @param resource datos de creación de la zona
     * @return la zona de cocina creada
     */
    @PostMapping
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<KitchenZoneResource> createZone(@RequestBody CreateKitchenZoneResource resource) {
        KitchenZone zone = commandService.createZone(resource.name());
        return ResponseEntity.ok(
                KitchenZoneResourceFromEntityAssembler.toResourceFromEntity(zone)
        );
    }

    /**
     * Modifica el nombre de una zona de cocina existente.
     *
     * @param id       ID de la zona
     * @param resource nuevos datos
     * @return la zona actualizada
     */
    @PutMapping("/{id}")
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<KitchenZoneResource> updateZone(
            @PathVariable Long id,
            @RequestBody CreateKitchenZoneResource resource
    ) {
        KitchenZone zone = commandService.updateZone(id, resource.name());
        return ResponseEntity.ok(
                KitchenZoneResourceFromEntityAssembler.toResourceFromEntity(zone)
        );
    }

    /**
     * Elimina una zona de cocina.
     *
     * @param id ID de la zona
     * @return respuesta HTTP exitosa
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("catalog.edit_kitchen_zones")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        commandService.deleteZone(id);
        return ResponseEntity.ok().build();
    }
}
