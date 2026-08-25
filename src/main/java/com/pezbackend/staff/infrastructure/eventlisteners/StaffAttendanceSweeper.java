package com.pezbackend.staff.infrastructure.eventlisteners;

import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.events.UnresolvedAttendanceDetected;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.AttendanceRecordRepository;
import com.pezbackend.tenancy.domain.model.entities.OperationalConfig;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.OperationalConfigRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sweeper que ejecuta la lógica de detección de asistencias sin salida en una transacción.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StaffAttendanceSweeper {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final OperationalConfigRepository operationalConfigRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processUnresolvedCheckins(Long restaurantId) {
        if (restaurantId == null) {
            log.warn("processUnresolvedCheckins: Omitiendo proceso por restaurantId nulo.");
            return;
        }

        try {
            // Establecer el TenantContext de forma explícita para asegurar que el filtro de inquilino funcione
            TenantContext.setCurrentTenantId(restaurantId);

            List<AttendanceRecord> activeCheckins = attendanceRecordRepository.findAllByCheckOutAtIsNull();
            log.info("processUnresolvedCheckins: Detectados {} registros sin salida para el tenant {}", activeCheckins.size(), restaurantId);

            if (activeCheckins.isEmpty()) {
                return;
            }

            // Marcar todos los checkins activos como no resueltos
            for (AttendanceRecord record : activeCheckins) {
                record.setUnresolved(true);
                attendanceRecordRepository.save(record);
            }

            // Consultar la preferencia de notificación operativa del restaurante
            OperationalConfig config = operationalConfigRepository.findAll().stream()
                    .findFirst()
                    .orElseGet(() -> new OperationalConfig(3, 0, 15, 30));

            String pref = config.getUnresolvedAttendanceNotificationPref();
            if (pref == null) pref = "BOTH";

            List<String> notifiedRoles = switch (pref) {
                case "ADMIN" -> List.of("ADMIN");
                case "CASHIER" -> List.of("CASHIER");
                default -> List.of("ADMIN", "CASHIER");
            };

            // Publicar el evento de dominio para que el realtime broker notifique via WebSockets
            for (AttendanceRecord record : activeCheckins) {
                UnresolvedAttendanceDetected alert = new UnresolvedAttendanceDetected(
                        record.getId(),
                        record.getStaffProfileId(),
                        record.getCheckInAt(),
                        restaurantId,
                        notifiedRoles
                );
                eventPublisher.publishEvent(alert);
                log.info("processUnresolvedCheckins: Emitido alerta UnresolvedAttendanceDetected para registro {}", record.getId());
            }

        } catch (Exception e) {
            log.error("processUnresolvedCheckins: Error al procesar asistencias sin salida para tenant {}: {}", restaurantId, e.getMessage(), e);
        } finally {
            // Limpiar el TenantContext
            TenantContext.clear();
        }
    }
}
