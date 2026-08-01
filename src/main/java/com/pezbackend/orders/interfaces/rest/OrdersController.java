package com.pezbackend.orders.interfaces.rest;

import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.services.OrderCommandService;
import com.pezbackend.orders.domain.services.OrderQueryService;
import com.pezbackend.orders.interfaces.rest.resources.*;
import com.pezbackend.orders.interfaces.rest.transform.OrderItemResourceFromEntityAssembler;
import com.pezbackend.orders.interfaces.rest.transform.OrderResourceFromEntityAssembler;
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
 * Controlador REST para la gestión integral de comandas (pedidos), colas de atención/cocina,
 * adición y anulación de platos y ajustes de precio.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints para la gestión de comandas y colas de atención/cocina")
public class OrdersController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResource>> getAllOrders() {
        List<Order> orders = orderQueryService.getAllOrders();
        List<OrderResource> resources = orders.stream()
                .map(OrderResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResource> getOrderById(@PathVariable Long id) {
        Order order = orderQueryService.getOrderById(id);
        return ResponseEntity.ok(OrderResourceFromEntityAssembler.toResourceFromEntity(order));
    }

    @GetMapping("/queue/attention")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RestaurantTableResource>> getAttentionQueue() {
        List<RestaurantTable> queue = orderQueryService.getAttentionQueue();
        List<RestaurantTableResource> resources = queue.stream()
                .map(RestaurantTableResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/queue/kitchen")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderItemResource>> getKitchenQueue(@RequestParam(required = false) Long zoneId) {
        List<OrderItem> queue = orderQueryService.getKitchenQueue(zoneId);
        List<OrderItemResource> resources = queue.stream()
                .map(OrderItemResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    @RequiresPermission("orders.create")
    public ResponseEntity<OrderResource> createOrder(@Valid @RequestBody CreateOrderResource resource) {
        Order order = orderCommandService.createOrder(
                resource.tableId(),
                resource.type(),
                resource.customerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResourceFromEntityAssembler.toResourceFromEntity(order));
    }

    @PostMapping("/{id}/items")
    @RequiresPermission("orders.modify_item")
    public ResponseEntity<Void> addItems(
            @PathVariable Long id,
            @Valid @RequestBody AddOrderItemResource resource
    ) {
        String waiter = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.addItemsToOrder(
                id,
                resource.productId(),
                resource.quantity(),
                resource.note(),
                resource.waiterId(),
                waiter
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/items/{itemId}/increase")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> increaseItemQuantity(
            @PathVariable Long orderId,
            @PathVariable Long itemId
    ) {
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.increaseItemQuantity(orderId, itemId, executor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/items/{itemId}/decrease")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> decreaseItemQuantity(
            @PathVariable Long orderId,
            @PathVariable Long itemId
    ) {
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.decreaseItemQuantity(orderId, itemId, executor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/items/{itemId}/cancel")
    @RequiresPermission("orders.cancel_item")
    public ResponseEntity<Void> cancelItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam(required = false) String cancellationReason,
            @RequestParam(required = false) String detail,
            @RequestBody(required = false) CancelOrderItemResource body
    ) {
        String reason = cancellationReason;
        String reasonDetail = detail;
        if (body != null) {
            if (reason == null) reason = body.cancellationReason();
            if (reasonDetail == null) reasonDetail = body.detail();
        }
        if (reason == null || reason.strip().isEmpty()) {
            throw new com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException(
                    "REASON_REQUIRED", "El motivo de la cancelación es obligatorio."
            );
        }
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.cancelItem(orderId, itemId, reason, reasonDetail, executor);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @RequiresPermission("orders.delete_item")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam(required = false) String cancellationReason,
            @RequestParam(required = false) String detail,
            @RequestBody(required = false) CancelOrderItemResource body
    ) {
        String reason = cancellationReason;
        String reasonDetail = detail;
        if (body != null) {
            if (reason == null) reason = body.cancellationReason();
            if (reasonDetail == null) reasonDetail = body.detail();
        }
        if (reason == null || reason.strip().isEmpty()) {
            throw new com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException(
                    "REASON_REQUIRED", "El motivo de la eliminación es obligatorio."
            );
        }
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.deleteItem(orderId, itemId, reason, reasonDetail, executor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/items/{itemId}/status")
    @RequiresPermission("orders.modify_item")
    public ResponseEntity<Void> changeItemStatus(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam String status
    ) {
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.changeItemStatus(orderId, itemId, status, executor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/price-adjustment")
    @RequiresPermission("orders.adjust_price")
    public ResponseEntity<Void> applyPriceAdjustment(
            @PathVariable Long id,
            @Valid @RequestBody ApplyPriceAdjustmentResource resource
    ) {
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.applyPriceAdjustment(
                id,
                resource.scope(),
                resource.validity(),
                resource.startAt(),
                resource.endAt(),
                resource.newValue(),
                resource.reason(),
                executor
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/issue-receipt")
    @RequiresPermission("orders.issue_receipt")
    public ResponseEntity<Void> issueReceipt(@PathVariable Long id) {
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        orderCommandService.issueReceipt(id, executor);
        return ResponseEntity.noContent().build();
    }
}
