package com.pezbackend.inventory.application.internal.services;

import com.pezbackend.catalog.domain.model.entities.Recipe;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.RecipeRepository;
import com.pezbackend.inventory.domain.model.entities.StockMovement;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.model.entities.StockLevel;
import com.pezbackend.inventory.domain.model.events.LowStockAlertTriggered;
import com.pezbackend.inventory.domain.model.events.SupplyDeductedBySale;
import com.pezbackend.inventory.domain.model.events.StockMismatchDetected;
import com.pezbackend.inventory.domain.model.valueobjects.StockMovementType;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.StockMovementRepository;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.orders.domain.model.events.ItemStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;

/**
 * Escuchador de eventos del ciclo de vida de comandas (Bounded Context orders).
 * Descuenta de forma automática los insumos del almacén cuando un plato es marcado como entregado (DELIVERED).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final RecipeRepository recipeRepository;
    private final SupplyRepository supplyRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Reacciona cuando un ítem cambia de estado. Si transiciona a DELIVERED, realiza el descuento.
     * Corre en una transacción independiente REQUIRES_NEW para garantizar la persistencia del movimiento de stock,
     * dado que AFTER_COMMIT se ejecuta fuera del alcance de la transacción origen ya cerrada.
     *
     * @param event el evento de cambio de estado de preparación
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleItemStatusChanged(ItemStatusChangedEvent event) {
        log.info("Inventory: Received ItemStatusChangedEvent for order {}, item {}, product {}, qty {}, status {}.",
                event.orderId(), event.itemId(), event.productId(), event.quantity(), event.newStatus());

        if (!"DELIVERED".equalsIgnoreCase(event.newStatus())) {
            return;
        }

        List<Recipe> recipeItems = recipeRepository.findAllByProductId(event.productId());
        if (recipeItems.isEmpty()) {
            log.info("Inventory: No recipe defined for product ID {}. Skipping stock deduction.", event.productId());
            return;
        }

        for (Recipe recipeItem : recipeItems) {
            BigDecimal qtyUsed = recipeItem.getQuantityUsed();
            BigDecimal totalDeduction = qtyUsed.multiply(new BigDecimal(event.quantity()));

            supplyRepository.findById(recipeItem.getSupplyId()).ifPresentOrElse(
                    supply -> {
                        BigDecimal oldStock = supply.getCurrentStock();
                        BigDecimal newStock = oldStock.subtract(totalDeduction);
                        supply.setCurrentStock(newStock);
                        supplyRepository.save(supply);

                        // Registrar movimiento de stock (SALE_DEDUCTION)
                        StockMovement movement = new StockMovement(
                                supply.getId(),
                                StockMovementType.SALE_DEDUCTION,
                                totalDeduction,
                                event.executorUsername() != null ? event.executorUsername() : "system",
                                "Descuento automático por venta de plato en pedido ID: " + event.orderId()
                        );
                        stockMovementRepository.save(movement);

                        log.info("Inventory: Deducted {} of {} (old stock: {}, new stock: {}).",
                                totalDeduction, supply.getName(), oldStock, newStock);

                        // Publicar evento de descuento
                        eventPublisher.publishEvent(new SupplyDeductedBySale(supply.getId(), event.productId(), totalDeduction, newStock));

                        // Publicar alerta de stock bajo
                        StockLevel oldLevel = supply.getStockLevelFor(oldStock);
                        StockLevel newLevel = supply.getStockLevelFor(newStock);
                        if (oldLevel != newLevel && (newLevel == StockLevel.AGOTADO || newLevel == StockLevel.CRITICO || newLevel == StockLevel.BAJO)) {
                            if (newLevel.ordinal() < oldLevel.ordinal()) {
                                log.warn("Inventory: Low stock alert triggered for supply {} (Level: {}).", supply.getName(), newLevel);
                                eventPublisher.publishEvent(new LowStockAlertTriggered(supply.getId(), supply.getName(), newStock, supply.getMinThreshold(), newLevel));
                            }
                        }

                        // Publicar descuadre si el stock queda en negativo
                        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                            log.warn("Inventory: Stock mismatch detected for supply {} (negative stock: {}).", supply.getName(), newStock);
                            eventPublisher.publishEvent(new StockMismatchDetected(supply.getId(), supply.getName(), totalDeduction, oldStock));
                        }
                    },
                    () -> log.error("Inventory: Supply ID {} in recipe of product {} was not found in database.",
                            recipeItem.getSupplyId(), event.productId())
            );
        }
    }
}
