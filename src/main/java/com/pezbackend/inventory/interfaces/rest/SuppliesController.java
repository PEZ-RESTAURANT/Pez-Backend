package com.pezbackend.inventory.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.services.SupplyCommandService;
import com.pezbackend.inventory.domain.services.SupplyQueryService;
import com.pezbackend.inventory.interfaces.rest.resources.CreateSupplyResource;
import com.pezbackend.inventory.interfaces.rest.resources.SupplyResource;
import com.pezbackend.inventory.interfaces.rest.resources.UpdateSupplyResource;
import com.pezbackend.inventory.interfaces.rest.transform.SupplyResourceFromEntityAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de insumos (Supply) en el almacén.
 */
@RestController
@RequestMapping("/api/v1/supplies")
@RequiredArgsConstructor
public class SuppliesController {

    private final SupplyCommandService supplyCommandService;
    private final SupplyQueryService supplyQueryService;

    /**
     * Obtiene la lista de todos los insumos.
     *
     * @return ResponseEntity con la lista de insumos
     */
    @GetMapping
    @RequiresPermission("inventory.view")
    public ResponseEntity<List<SupplyResource>> getAll() {
        List<Supply> supplies = supplyQueryService.getAllSupplies();
        return ResponseEntity.ok(
                supplies.stream()
                        .map(SupplyResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    /**
     * Obtiene los detalles de un insumo por su ID.
     *
     * @param id ID del insumo
     * @return ResponseEntity con el insumo
     */
    @GetMapping("/{id}")
    @RequiresPermission("inventory.view")
    public ResponseEntity<SupplyResource> getById(@PathVariable Long id) {
        return supplyQueryService.getSupplyById(id)
                .map(SupplyResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo insumo.
     *
     * @param resource datos del nuevo insumo
     * @return ResponseEntity con el insumo creado
     */
    @PostMapping
    @RequiresPermission("catalog.edit_supplies_recipes")
    public ResponseEntity<SupplyResource> create(@RequestBody CreateSupplyResource resource) {
        Supply supply = supplyCommandService.createSupply(resource.name(), resource.unit(), resource.minThreshold());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SupplyResourceFromEntityAssembler.toResourceFromEntity(supply));
    }

    /**
     * Actualiza los datos de un insumo.
     *
     * @param id       ID del insumo
     * @param resource nuevos datos
     * @return ResponseEntity con el insumo actualizado
     */
    @PutMapping("/{id}")
    @RequiresPermission("catalog.edit_supplies_recipes")
    public ResponseEntity<SupplyResource> update(@PathVariable Long id, @RequestBody UpdateSupplyResource resource) {
        Supply supply = supplyCommandService.updateSupply(id, resource.name(), resource.unit(), resource.minThreshold());
        return ResponseEntity.ok(SupplyResourceFromEntityAssembler.toResourceFromEntity(supply));
    }

    /**
     * Elimina un insumo.
     *
     * @param id ID del insumo
     * @return ResponseEntity vacío
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("catalog.edit_supplies_recipes")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplyCommandService.deleteSupply(id);
        return ResponseEntity.ok().build();
    }
}
