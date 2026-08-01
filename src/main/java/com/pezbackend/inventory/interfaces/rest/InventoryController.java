package com.pezbackend.inventory.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.services.SupplyCommandService;
import com.pezbackend.inventory.domain.services.SupplyQueryService;
import com.pezbackend.inventory.interfaces.rest.resources.AdjustManualResource;
import com.pezbackend.inventory.interfaces.rest.resources.RestockResource;
import com.pezbackend.inventory.interfaces.rest.resources.StockMovementResource;
import com.pezbackend.inventory.interfaces.rest.resources.SupplyResource;
import com.pezbackend.inventory.interfaces.rest.transform.StockMovementResourceFromEntityAssembler;
import com.pezbackend.inventory.interfaces.rest.transform.SupplyResourceFromEntityAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de inventario, movimientos de stock y alertas.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final SupplyCommandService supplyCommandService;
    private final SupplyQueryService supplyQueryService;

    /**
     * Obtiene el historial de movimientos de stock para un insumo determinado.
     *
     * @param id ID del insumo
     * @return ResponseEntity con la lista de movimientos
     */
    @GetMapping("/supplies/{id}/movements")
    @RequiresPermission("inventory.view")
    public ResponseEntity<List<StockMovementResource>> getMovements(@PathVariable Long id) {
        List<StockMovement> movements = supplyQueryService.getMovementsForSupply(id);
        return ResponseEntity.ok(
                movements.stream()
                        .map(StockMovementResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    /**
     * Registra un ingreso de insumos (reabastecimiento).
     *
     * @param id       ID del insumo
     * @param resource DTO con la cantidad a ingresar
     * @return ResponseEntity vacío
     */
    @PostMapping("/supplies/{id}/restock")
    @RequiresPermission("inventory.restock")
    public ResponseEntity<Void> restock(
            @PathVariable Long id,
            @RequestBody RestockResource resource
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        supplyCommandService.restock(id, resource.quantity(), username);
        return ResponseEntity.ok().build();
    }

    /**
     * Realiza un ajuste de stock manual.
     *
     * @param id       ID del insumo
     * @param resource DTO con la cantidad y motivo
     * @return ResponseEntity vacío
     */
    @PostMapping("/supplies/{id}/adjust")
    @RequiresPermission("inventory.adjust_manual")
    public ResponseEntity<Void> adjust(
            @PathVariable Long id,
            @RequestBody AdjustManualResource resource
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        supplyCommandService.adjustManual(id, resource.quantity(), resource.reason(), username);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene la lista de insumos bajo el umbral mínimo (alerta de stock bajo).
     *
     * @return ResponseEntity con la lista de insumos
     */
    @GetMapping("/alerts/low-stock")
    @RequiresPermission("inventory.view")
    public ResponseEntity<List<SupplyResource>> getLowStockAlerts() {
        List<Supply> supplies = supplyQueryService.getLowStockSupplies();
        return ResponseEntity.ok(
                supplies.stream()
                        .map(SupplyResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    /**
     * Obtiene la lista de insumos con stock negativo (descuadres de inventario).
     *
     * @return ResponseEntity con la lista de insumos
     */
    @GetMapping("/alerts/mismatched")
    @RequiresPermission("inventory.view")
    public ResponseEntity<List<SupplyResource>> getMismatchedAlerts() {
        List<Supply> supplies = supplyQueryService.getMismatchedSupplies();
        return ResponseEntity.ok(
                supplies.stream()
                        .map(SupplyResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }
}
