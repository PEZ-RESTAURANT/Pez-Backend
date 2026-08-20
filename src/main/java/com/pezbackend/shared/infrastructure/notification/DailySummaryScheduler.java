package com.pezbackend.shared.infrastructure.notification;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.entities.CashRegisterMismatch;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterMismatchRepository;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.inventory.domain.model.entities.Supply;
import com.pezbackend.inventory.domain.model.entities.StockLevel;
import com.pezbackend.inventory.infrastructure.persistence.jpa.repositories.SupplyRepository;
import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import com.pezbackend.tenancy.domain.model.entities.OperationalConfig;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.OperationalConfigRepository;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.AttendanceRecordRepository;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.StaffProfileRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tarea programada que ejecuta el consolidado diario operativo de cada local y lo despacha a los administradores.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailySummaryScheduler {

    private final OperationalConfigRepository operationalConfigRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final CashRegisterMismatchRepository cashRegisterMismatchRepository;
    private final SupplyRepository supplyRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final EmailNotificationChannel emailNotificationChannel;

    @Scheduled(cron = "0 * * * * ?")
    public void sendDailySummaries() {
        log.debug("DailySummaryScheduler: Running scheduled summary check...");

        List<OperationalConfig> configs = operationalConfigRepository.findAll();

        for (OperationalConfig config : configs) {
            Long restaurantId = config.getRestaurantId();
            if (restaurantId == null) continue;

            LocalDate today = LocalDate.now();
            if (config.getLastDailySummarySentAt() != null && config.getLastDailySummarySentAt().equals(today)) {
                continue;
            }

            LocalTime nowTime = LocalTime.now();
            LocalTime summaryTime = LocalTime.parse(config.getDailySummaryTime() != null ? config.getDailySummaryTime() : "22:00");

            if (nowTime.isBefore(summaryTime)) {
                continue;
            }

            log.info("DailySummaryScheduler: Triggering daily summary for tenant ID: {}", restaurantId);

            TenantContext.setCurrentTenantId(restaurantId);
            try {
                sendDailySummaryForTenant(restaurantId);

                config.setLastDailySummarySentAt(today);
                operationalConfigRepository.save(config);
            } catch (Exception e) {
                log.error("DailySummaryScheduler: Failed to send daily summary for tenant: {}", restaurantId, e);
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void sendDailySummaryForTenant(Long restaurantId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        // 1. Cash Registers
        List<CashRegister> registers = cashRegisterRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(startOfToday) && r.getCreatedAt().isBefore(endOfToday))
                .toList();

        List<String> cashRegisterLines = new ArrayList<>();
        if (registers.isEmpty()) {
            cashRegisterLines.add("No se registraron movimientos de caja el día de hoy.");
        } else {
            for (CashRegister r : registers) {
                String statusText = r.getStatus().toString();
                String closedAtText = r.getClosedAt() != null ? r.getClosedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "Abierta";
                
                String mismatchText = "";
                Optional<CashRegisterMismatch> mismatchOpt = cashRegisterMismatchRepository.findByCashRegisterId(r.getId());
                if (mismatchOpt.isPresent()) {
                    CashRegisterMismatch m = mismatchOpt.get();
                    java.math.BigDecimal diff = m.getExpectedAmount().subtract(m.getDeclaredAmount()).abs().setScale(2, java.math.RoundingMode.HALF_UP);
                    mismatchText = " (Diferencia: S/ " + diff + " " + m.getStatus() + ")";
                }

                cashRegisterLines.add(String.format("• Caja #%d: Saldo Inicial: S/ %.2f | Estado: %s | Cierre: %s%s",
                        r.getId(), r.getOpeningBalance(), statusText, closedAtText, mismatchText));
            }
        }

        // 2. Supply stock alerts
        List<Supply> lowSupplies = supplyRepository.findAll().stream()
                .filter(s -> s.getStockLevel() == StockLevel.BAJO || s.getStockLevel() == StockLevel.CRITICO || s.getStockLevel() == StockLevel.AGOTADO)
                .toList();

        List<String> stockLines = new ArrayList<>();
        if (lowSupplies.isEmpty()) {
            stockLines.add("Todos los insumos se encuentran en niveles estables.");
        } else {
            for (Supply s : lowSupplies) {
                stockLines.add(String.format("• %s: Stock: %s | Mínimo: %s | Estado: %s",
                        s.getName(), s.getCurrentStock(), s.getMinThreshold(), s.getStockLevel()));
            }
        }

        // 3. Absent Staff
        List<StaffProfile> profiles = staffProfileRepository.findAll();
        List<String> absentLines = new ArrayList<>();
        List<AttendanceRecord> todayAttendances = attendanceRecordRepository.findAll().stream()
                .filter(a -> a.getCheckInAt() != null && a.getCheckInAt().isAfter(startOfToday) && a.getCheckInAt().isBefore(endOfToday))
                .toList();

        for (StaffProfile profile : profiles) {
            User u = userRepository.findById(profile.getAccountId()).orElse(null);
            if (u == null || !Boolean.TRUE.equals(u.getActive())) {
                continue;
            }

            boolean attended = todayAttendances.stream().anyMatch(a -> a.getStaffProfileId().equals(profile.getId()));
            if (!attended) {
                absentLines.add(String.format("• %s (ID: #%d)", u.getFullName(), profile.getId()));
            }
        }
        if (absentLines.isEmpty()) {
            absentLines.add("Todos los empleados activos registraron asistencia el día de hoy.");
        }

        // 4. Annulments
        List<AuditEvent> annulments = auditEventRepository.findAll().stream()
                .filter(a -> "ItemCancelled".equals(a.getEventType()))
                .filter(a -> a.getTimestamp() != null && a.getTimestamp().isAfter(startOfToday) && a.getTimestamp().isBefore(endOfToday))
                .toList();

        List<String> annulmentLines = new ArrayList<>();
        if (annulments.isEmpty()) {
            annulmentLines.add("No se registraron anulaciones de platos hoy.");
        } else {
            for (AuditEvent a : annulments) {
                Map<String, Object> payload = a.getPayload();
                Object productVal = payload.get("productId");
                Object qtyVal = payload.get("quantity");
                Object reasonVal = payload.get("cancellationReason");
                annulmentLines.add(String.format("• Producto ID #%s | Cantidad: %s | Motivo: %s | Anulado por: %s",
                        productVal, qtyVal, reasonVal, a.getUserId()));
            }
        }

        List<String> adminEmails = userRepository.findAll().stream()
                .filter(User::getActive)
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == com.pezbackend.iam.domain.model.valueobjects.Roles.ADMIN))
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();

        if (adminEmails.isEmpty()) return;

        String subject = "📅 Resumen Operativo Diario — Al Toque";
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Resumen Operativo Diario");
        model.put("subtitle", "Al Toque - Auditoría del Local");
        model.put("greeting", "Estimado Administrador:");
        model.put("paragraphs", List.of(
                "A continuación, se detalla el reporte consolidado de las operaciones ocurridas hoy en el local."
        ));

        Map<String, Object> keyDetails = new LinkedHashMap<>();
        keyDetails.put("Cajas Registradas", String.join("\n", cashRegisterLines));
        keyDetails.put("Insumos Alertas", String.join("\n", stockLines));
        keyDetails.put("Personal Ausente*", String.join("\n", absentLines));
        keyDetails.put("Anulaciones del Día", String.join("\n", annulmentLines));
        
        model.put("keyDetails", keyDetails);
        model.put("alertTitle", "⚠️ NOTA IMPORTANTE:");
        model.put("alertText", "El listado de Personal Ausente incluye a todos los empleados activos sin marcas de asistencia registradas hoy. Sin un módulo de horarios asignados, el sistema no puede distinguir descansos o permisos de inasistencias.");

        for (String email : adminEmails) {
            emailNotificationChannel.send(email, subject, "email-template", model);
        }
    }
}
