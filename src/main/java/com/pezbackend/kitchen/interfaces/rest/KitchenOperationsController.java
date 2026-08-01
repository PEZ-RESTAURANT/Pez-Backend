package com.pezbackend.kitchen.interfaces.rest;

import com.pezbackend.kitchen.domain.services.KitchenZoneQueryService;
import com.pezbackend.catalog.domain.services.ProductKitchenZoneQueryService;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.kitchen.interfaces.rest.resources.KitchenQueueItemResource;
import com.pezbackend.kitchen.interfaces.rest.transform.KitchenQueueItemResourceFromEntityAssembler;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controlador REST para las operaciones de preparación en la cocina (cola FIFO y transiciones).
 */
@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
public class KitchenOperationsController {

    private final KitchenZoneQueryService kitchenZoneQueryService;
    private final ProductKitchenZoneQueryService productKitchenZoneQueryService;
    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    /**
     * Obtiene la cola FIFO de platos/ítems pendientes de preparación para una zona específica.
     *
     * @param zoneId ID de la zona de cocina
     * @return lista de ítems en cola de preparación para esa zona
     */
    @GetMapping("/zones/{zoneId}/queue")
    @RequiresPermission("kitchen.view_own_zone")
    public ResponseEntity<List<KitchenQueueItemResource>> getKitchenQueueByZone(@PathVariable Long zoneId) {
        // 1. Validar que la zona exista
        kitchenZoneQueryService.getZoneById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("KITCHEN_ZONE_NOT_FOUND", "Zona de cocina no encontrada con ID: " + zoneId));

        // 2. Obtener la lista completa de ítems en cocina (sin filtrar)
        List<OrderItem> fullQueue = orderQueryService.getKitchenQueue(null);

        // 3. Obtener los IDs de productos asociados a esta zona de cocina
        Set<Long> productIdsInZone = productKitchenZoneQueryService.getProductIdsForZone(zoneId);

        // 4. Filtrar y mapear a recursos
        List<KitchenQueueItemResource> filteredQueue = fullQueue.stream()
                .filter(item -> productIdsInZone.contains(item.getProductId()))
                .map(KitchenQueueItemResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(filteredQueue);
    }

    /**
     * Inicia la preparación de un plato específico (cambio de estado PENDING a IN_PREPARATION).
     *
     * @param itemId ID del ítem de comanda (OrderItem)
     * @return respuesta HTTP vacía exitosa (No Content)
     */
    @PostMapping("/items/{itemId}/start-preparation")
    @RequiresPermission("kitchen.change_item_status")
    public ResponseEntity<Void> startPreparation(@PathVariable Long itemId) {
        // 1. Encontrar la comanda que contiene el ítem
        Order order = findOrderContainingItem(itemId);

        // 2. Obtener el usuario ejecutor actual
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Ejecutar la transición de estado delegando en orders
        orderCommandService.changeItemStatus(order.getId(), itemId, "IN_PREPARATION", executor);

        return ResponseEntity.noContent().build();
    }

    /**
     * Marca un plato como listo/preparado (cambio de estado IN_PREPARATION a READY).
     *
     * @param itemId ID del ítem de comanda (OrderItem)
     * @return respuesta HTTP vacía exitosa (No Content)
     */
    @PostMapping("/items/{itemId}/mark-ready")
    @RequiresPermission("kitchen.change_item_status")
    public ResponseEntity<Void> markReady(@PathVariable Long itemId) {
        // 1. Encontrar la comanda que contiene el ítem
        Order order = findOrderContainingItem(itemId);

        // 2. Obtener el usuario ejecutor actual
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Ejecutar la transición de estado delegando en orders
        orderCommandService.changeItemStatus(order.getId(), itemId, "READY", executor);

        return ResponseEntity.noContent().build();
    }

    private Order findOrderContainingItem(Long itemId) {
        return orderQueryService.getAllOrders().stream()
                .filter(o -> o.getItems().stream().anyMatch(i -> i.getId().equals(itemId)))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ITEM_NOT_FOUND", "Ítem de comanda no encontrado con ID: " + itemId));
    }
}
