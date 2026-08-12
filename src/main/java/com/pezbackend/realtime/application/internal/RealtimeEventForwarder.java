package com.pezbackend.realtime.application.internal;

import com.pezbackend.shared.domain.model.DomainEvent;
import com.pezbackend.shared.infrastructure.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

/**
 * Listener asíncrono desacoplado que intercepta eventos de dominio después del commit
 * y los propaga hacia el tópico WebSocket correspondiente si pertenecen al catálogo de allowlist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RealtimeEventForwarder {

    private final SimpMessagingTemplate messagingTemplate;

    private static final Set<String> TABLES_EVENTS = Set.of(
            "TableAttentionRequested", "TableAttended", "AllItemsDelivered", "ReceiptIssued",
            "TableReleased", "PriceAdjustmentApplied", "TablesMerged", "TablesUnmerged", "OrderTransferred"
    );

    private static final Set<String> KITCHEN_EVENTS = Set.of(
            "ItemOrdered", "ItemStatusChanged", "ItemCancelled"
    );

    private static final Set<String> ALERTS_EVENTS = Set.of(
            "LowStockAlertTriggered", "StockMismatchDetected", "CashRegisterMismatched", "ForcedCloseByCutoff"
    );

    /**
     * Captura los eventos de dominio y los reenvía asíncronamente al broker WebSocket.
     */
    @Async("realtimeEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void forwardEvent(DomainEvent event) {
        String eventType = event.eventType();
        
        // El reenvío en tiempo real requiere saber el restaurantId
        Long restaurantId = TenantContext.getCurrentTenantId();

        // Si no está seteado en el contexto del hilo actual, intentamos extraerlo del principal autenticado
        if (restaurantId == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
                restaurantId = userDetails.getRestaurantId();
            }
        }

        if (restaurantId == null) {
            log.warn("WebSocket Reenvío: Omitiendo evento '{}' por no poseer restaurantId en contexto.", eventType);
            return;
        }

        String topic = null;
        if (TABLES_EVENTS.contains(eventType)) {
            topic = String.format("/topic/restaurants/%d/tables", restaurantId);
        } else if (KITCHEN_EVENTS.contains(eventType)) {
            topic = String.format("/topic/restaurants/%d/kitchen", restaurantId);
        } else if (ALERTS_EVENTS.contains(eventType)) {
            topic = String.format("/topic/restaurants/%d/alerts", restaurantId);
        }

        if (topic != null) {
            log.info("WebSocket Reenvío: Propagando evento '{}' al tópico '{}'", eventType, topic);
            try {
                messagingTemplate.convertAndSend(topic, event);
            } catch (Exception e) {
                log.error("WebSocket Reenvío: Fallo al emitir evento '{}': {}", eventType, e.getMessage());
            }
        }
    }
}
