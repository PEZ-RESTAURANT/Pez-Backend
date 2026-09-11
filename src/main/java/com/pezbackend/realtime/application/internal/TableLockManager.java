package com.pezbackend.realtime.application.internal;

import com.pezbackend.realtime.domain.model.TableLock;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor en memoria que controla el ciclo de vida de los bloqueos temporales de mesas.
 * Publica eventos a través de WebSocket cuando ocurre un bloqueo o liberación.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TableLockManager {

    private final SimpMessagingTemplate messagingTemplate;

    // Mapa multinivel: restaurantId -> tableId -> TableLock
    private final Map<Long, Map<Long, TableLock>> locks = new ConcurrentHashMap<>();

    /**
     * Obtiene los bloqueos activos de mesas para un restaurante específico.
     */
    public Map<Long, TableLock> getActiveLocks(Long restaurantId) {
        return locks.getOrDefault(restaurantId, java.util.Collections.emptyMap());
    }

    /**
     * Bloquea una mesa temporalmente para un mozo. Si la mesa ya está bloqueada por otro
     * mozo y el bloqueo está vigente (menos de 3 minutos), lanza una excepción.
     */
    public void lockTable(Long restaurantId, Long tableId, Long waiterId, String waiterName) {
        Map<Long, TableLock> restaurantLocks = locks.computeIfAbsent(restaurantId, k -> new ConcurrentHashMap<>());
        TableLock existingLock = restaurantLocks.get(tableId);

        if (existingLock != null) {
            // Verificar si el bloqueo existente pertenece a otro mozo y sigue vigente (menos de 3 minutos)
            if (!existingLock.waiterId().equals(waiterId) && existingLock.lockedAt().isAfter(LocalDateTime.now().minusMinutes(3))) {
                throw new BusinessRuleViolationException(
                        "TABLE_LOCKED",
                        "Mesa en uso por " + existingLock.waiterName()
                );
            }
        }

        // Crear o refrescar bloqueo
        TableLock newLock = new TableLock(tableId, waiterId, waiterName, LocalDateTime.now());
        restaurantLocks.put(tableId, newLock);
        log.info("Mesa {} bloqueada en memoria para el mozo '{}' (Tenant {})", tableId, waiterName, restaurantId);

        // Notificar en tiempo real a los clientes de ese restaurante
        broadcastLockState(restaurantId, "TableLocked", Map.of("tableId", tableId, "waiterName", waiterName));
    }

    /**
     * Libera el bloqueo de una mesa si coincide con el mozo que la tenía bloqueada.
     */
    public void unlockTable(Long restaurantId, Long tableId, Long waiterId) {
        Map<Long, TableLock> restaurantLocks = locks.get(restaurantId);
        if (restaurantLocks == null) return;

        TableLock lock = restaurantLocks.get(tableId);
        if (lock != null && lock.waiterId().equals(waiterId)) {
            restaurantLocks.remove(tableId);
            log.info("Mesa {} desbloqueada en memoria por el mozo ID {} (Tenant {})", tableId, waiterId, restaurantId);
            broadcastLockState(restaurantId, "TableUnlocked", Map.of("tableId", tableId));
        }
    }

    /**
     * Libera todas las mesas bloqueadas por un mozo específico (ej. al cerrar sesión o desconexión).
     */
    public void unlockAllTablesForWaiter(Long restaurantId, Long waiterId) {
        Map<Long, TableLock> restaurantLocks = locks.get(restaurantId);
        if (restaurantLocks == null) return;

        restaurantLocks.forEach((tableId, lock) -> {
            if (lock.waiterId().equals(waiterId)) {
                restaurantLocks.remove(tableId);
                log.info("Mesa {} desbloqueada automáticamente por cierre de sesión del mozo ID {} (Tenant {})", tableId, waiterId, restaurantId);
                broadcastLockState(restaurantId, "TableUnlocked", Map.of("tableId", tableId));
            }
        });
    }

    /**
     * Libera de forma forzada un bloqueo de mesa sin validar el propietario (ej. acción de admin/cajero).
     */
    public void forceUnlockTable(Long restaurantId, Long tableId) {
        Map<Long, TableLock> restaurantLocks = locks.get(restaurantId);
        if (restaurantLocks == null) return;

        if (restaurantLocks.containsKey(tableId)) {
            restaurantLocks.remove(tableId);
            log.info("Mesa {} desbloqueada de forma forzada (Tenant {})", tableId, restaurantId);
            broadcastLockState(restaurantId, "TableUnlocked", Map.of("tableId", tableId));
        }
    }

    /**
     * Tarea programada que barre los bloqueos de mesas inactivos cada 30 segundos.
     * Cualquier bloqueo con más de 3 minutos de antigüedad es liberado de forma automática.
     */
    @Scheduled(fixedDelay = 30000)
    public void sweepExpiredLocks() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(3);

        locks.forEach((restaurantId, tableMap) -> {
            tableMap.forEach((tableId, lock) -> {
                if (lock.lockedAt().isBefore(expirationTime)) {
                    tableMap.remove(tableId);
                    log.info("Expiración automática: Mesa {} liberada por inactividad (Tenant {})", tableId, restaurantId);
                    broadcastLockState(restaurantId, "TableUnlocked", Map.of("tableId", tableId));
                }
            });
        });
    }

    private void broadcastLockState(Long restaurantId, String eventType, Map<String, Object> payload) {
        String topic = String.format("/topic/restaurants/%d/tables", restaurantId);
        System.out.println("DEBUG LOCK BROADCAST: Enviando '" + eventType + "' al tópico '" + topic + "'");
        try {
            messagingTemplate.convertAndSend(topic, (Object) Map.of(
                    "eventType", eventType,
                    "payload", payload
            ));
        } catch (Exception e) {
            System.err.println("DEBUG LOCK BROADCAST ERROR: " + e.getMessage());
            log.error("Fallo al propagar estado de bloqueo '{}' al tópico WebSocket: {}", eventType, e.getMessage());
        }
    }
}
