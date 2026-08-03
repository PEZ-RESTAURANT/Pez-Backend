package com.pezbackend.orders.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.interfaces.rest.resources.CreateTableResource;
import com.pezbackend.orders.interfaces.rest.resources.RestaurantTableResource;
import com.pezbackend.orders.interfaces.rest.resources.UpdateTablePositionResource;
import com.pezbackend.orders.interfaces.rest.resources.UpdateTableResource;
import com.pezbackend.orders.interfaces.rest.transform.RestaurantTableResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.pezbackend.orders.domain.model.entities.Reservation;
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
    private final com.pezbackend.orders.domain.services.ReservationQueryService reservationQueryService;
    private final com.pezbackend.realtime.application.internal.TableLockManager tableLockManager;

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

    @PutMapping("/{id}")
    @RequiresPermission("orders.edit_layout")
    public ResponseEntity<RestaurantTableResource> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTableResource resource
    ) {
        RestaurantTable table = orderCommandService.updateTableDetails(id, resource.number(), resource.floor(), resource.zoneTag());
        return ResponseEntity.ok(RestaurantTableResourceFromEntityAssembler.toResourceFromEntity(table));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("orders.edit_layout")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        orderCommandService.deleteTable(id);
        return ResponseEntity.ok().build();
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

    @PostMapping("/{anchorTableId}/merge")
    @RequiresPermission("orders.merge_tables")
    public ResponseEntity<Void> mergeTables(
            @PathVariable Long anchorTableId,
            @Valid @RequestBody com.pezbackend.orders.interfaces.rest.resources.MergeTablesResource resource
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.mergeTables(anchorTableId, resource.tableIds(), username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{anchorTableId}/unmerge")
    @RequiresPermission("orders.merge_tables")
    public ResponseEntity<Void> unmergeTables(@PathVariable Long anchorTableId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.unmergeTables(anchorTableId, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{fromTableId}/transfer-order")
    @RequiresPermission("orders.transfer_order")
    public ResponseEntity<Void> transferOrder(
            @PathVariable Long fromTableId,
            @Valid @RequestBody com.pezbackend.orders.interfaces.rest.resources.TransferOrderResource resource
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.transferOrder(fromTableId, resource.toTableId(), username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/merge-group")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RestaurantTableResource>> getMergeGroup(@PathVariable Long id) {
        List<RestaurantTable> group = orderQueryService.getMergeGroup(id);
        List<RestaurantTableResource> resources = group.stream()
                .map(RestaurantTableResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}/reservations")
    @RequiresPermission("reservations.view")
    public ResponseEntity<List<com.pezbackend.orders.interfaces.rest.resources.ReservationResource>> getTableReservations(@PathVariable Long id) {
        List<Reservation> reservations = reservationQueryService.getTableReservations(id);
        List<com.pezbackend.orders.interfaces.rest.resources.ReservationResource> resources = reservations.stream()
                .map(com.pezbackend.orders.interfaces.rest.transform.ReservationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> lockTable(@PathVariable Long id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
            Long restaurantId = com.pezbackend.shared.infrastructure.TenantContext.getCurrentTenantId();
            if (restaurantId == null) {
                restaurantId = userDetails.getRestaurantId();
            }
            tableLockManager.lockTable(restaurantId, id, userDetails.getId(), userDetails.getFullName());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlockTable(@PathVariable Long id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
            Long restaurantId = com.pezbackend.shared.infrastructure.TenantContext.getCurrentTenantId();
            if (restaurantId == null) {
                restaurantId = userDetails.getRestaurantId();
            }
            tableLockManager.unlockTable(restaurantId, id, userDetails.getId());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.Map<Long, com.pezbackend.realtime.domain.model.TableLock>> getActiveLocks() {
        Long restaurantId = com.pezbackend.shared.infrastructure.TenantContext.getCurrentTenantId();
        if (restaurantId == null) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
                restaurantId = userDetails.getRestaurantId();
            }
        }
        if (restaurantId != null) {
            return ResponseEntity.ok(tableLockManager.getActiveLocks(restaurantId));
        }
        return ResponseEntity.ok(java.util.Collections.emptyMap());
    }

    @PostMapping("/{id}/force-unlock")
    @RequiresPermission("orders.force_unlock_table")
    public ResponseEntity<Void> forceUnlockTable(@PathVariable Long id) {
        Long restaurantId = com.pezbackend.shared.infrastructure.TenantContext.getCurrentTenantId();
        if (restaurantId == null) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
                restaurantId = userDetails.getRestaurantId();
            }
        }
        if (restaurantId != null) {
            tableLockManager.forceUnlockTable(restaurantId, id);
        }
        return ResponseEntity.noContent().build();
    }
}
