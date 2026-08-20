package com.pezbackend.shared.infrastructure.notification;

import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.inventory.domain.model.events.LowStockAlertTriggered;
import com.pezbackend.inventory.domain.model.events.StockMismatchDetected;
import com.pezbackend.cashregister.domain.model.events.CashRegisterMismatched;
import com.pezbackend.cashregister.domain.model.events.ForcedCloseByCutoff;
import com.pezbackend.orders.domain.model.events.ItemCancelledEvent;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener desacoplado que intercepta eventos de dominio después del commit y gatilla notificaciones.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final SupplyRepository supplyRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLowStockAlert(LowStockAlertTriggered event) {
        Long restaurantId = TenantContext.getCurrentTenantId();
        if (restaurantId == null) {
            restaurantId = supplyRepository.findById(event.supplyId())
                    .map(supply -> supply.getRestaurantId())
                    .orElse(null);
        }
        if (restaurantId != null) {
            notificationService.processLowStockAlertAsync(restaurantId, event);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCashRegisterMismatched(CashRegisterMismatched event) {
        Long restaurantId = TenantContext.getCurrentTenantId();
        if (restaurantId == null) {
            restaurantId = cashRegisterRepository.findById(event.cashRegisterId())
                    .map(register -> register.getRestaurantId())
                    .orElse(null);
        }
        if (restaurantId != null) {
            notificationService.processCashRegisterMismatchedAsync(restaurantId, event);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleForcedCloseByCutoff(ForcedCloseByCutoff event) {
        Long restaurantId = TenantContext.getCurrentTenantId();
        if (restaurantId == null) {
            restaurantId = cashRegisterRepository.findById(event.cashRegisterId())
                    .map(register -> register.getRestaurantId())
                    .orElse(null);
        }
        if (restaurantId != null) {
            notificationService.processForcedCloseByCutoffAsync(restaurantId, event);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockMismatchDetected(StockMismatchDetected event) {
        Long restaurantId = TenantContext.getCurrentTenantId();
        if (restaurantId == null) {
            restaurantId = supplyRepository.findById(event.supplyId())
                    .map(supply -> supply.getRestaurantId())
                    .orElse(null);
        }
        if (restaurantId != null) {
            notificationService.processStockMismatchDetectedAsync(restaurantId, event);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleItemCancelled(ItemCancelledEvent event) {
        Long restaurantId = TenantContext.getCurrentTenantId();
        if (restaurantId == null) {
            restaurantId = orderRepository.findById(event.orderId())
                    .map(order -> order.getRestaurantId())
                    .orElse(null);
        }
        if (restaurantId != null) {
            String productName = productRepository.findById(event.productId())
                    .map(product -> product.getName())
                    .orElse(null);
            notificationService.processItemCancelledAsync(restaurantId, event, productName);
        }
    }
}
