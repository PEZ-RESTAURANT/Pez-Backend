package com.pezbackend.orders.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.interfaces.rest.resources.CreateTableResource;
import com.pezbackend.orders.interfaces.rest.resources.RestaurantTableResource;
import com.pezbackend.orders.interfaces.rest.resources.UpdateTablePositionResource;
import com.pezbackend.orders.interfaces.rest.transform.RestaurantTableResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar el diseño de mesas y peticiones de atención en el salón.
 */
@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@Tag(name = "Tables", description = "Endpoints para la distribución física e interacciones de mesas")
public class TablesController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RestaurantTableResource>> getAllTables() {
        List<RestaurantTable> tables = orderQueryService.getAllTables();
        List<RestaurantTableResource> resources = tables.stream()
                .map(RestaurantTableResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestaurantTableResource> getTableById(@PathVariable Long id) {
        RestaurantTable table = orderQueryService.getTableById(id);
        return ResponseEntity.ok(RestaurantTableResourceFromEntityAssembler.toResourceFromEntity(table));
    }

    @PostMapping
    @RequiresPermission("orders.edit_layout")
    public ResponseEntity<RestaurantTableResource> createTable(@Valid @RequestBody CreateTableResource resource) {
        RestaurantTable table = orderCommandService.createTable(
                resource.number(),
                resource.floor(),
                resource.zoneTag(),
                resource.positionX(),
                resource.positionY()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestaurantTableResourceFromEntityAssembler.toResourceFromEntity(table));
    }

    @PutMapping("/{id}/position")
    @RequiresPermission("orders.edit_layout")
    public ResponseEntity<RestaurantTableResource> updateTablePosition(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTablePositionResource resource
    ) {
        RestaurantTable table = orderCommandService.updateTablePosition(id, resource.positionX(), resource.positionY());
        return ResponseEntity.ok(RestaurantTableResourceFromEntityAssembler.toResourceFromEntity(table));
    }

    @PostMapping("/{id}/request-attention")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> requestAttention(@PathVariable Long id) {
        orderCommandService.requestAttention(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/attend")
    @RequiresPermission("orders.change_table_status")
    public ResponseEntity<Void> attendTable(@PathVariable Long id) {
        String waiter = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.attendTable(id, waiter);
        return ResponseEntity.noContent().build();
    }
}
