package com.pezbackend.shared.infrastructure.notification;

import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.inventory.domain.model.entities.StockLevel;
import com.pezbackend.inventory.domain.model.events.LowStockAlertTriggered;
import com.pezbackend.inventory.domain.model.events.StockMismatchDetected;
import com.pezbackend.cashregister.domain.model.events.CashRegisterMismatched;
import com.pezbackend.cashregister.domain.model.events.ForcedCloseByCutoff;
import com.pezbackend.orders.domain.model.events.ItemCancelledEvent;
import com.pezbackend.tenancy.domain.model.entities.OperationalConfig;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.OperationalConfigRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio encargado de procesar y enviar notificaciones asíncronas para eventos operativos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailNotificationChannel emailNotificationChannel;
    private final UserRepository userRepository;
    private final OperationalConfigRepository operationalConfigRepository;

    @Async
    public void processLowStockAlertAsync(Long restaurantId, LowStockAlertTriggered event) {
        // Distinción de stock: solo AGOTADO y CRITICO disparan correo instantáneo. BAJO se reserva para el consolidado diario.
        if (event.level() != StockLevel.AGOTADO && event.level() != StockLevel.CRITICO) {
            log.info("NotificationService: Stock status is {}, skipping instant email. It will be sent in daily summary.", event.level());
            return;
        }

        TenantContext.setCurrentTenantId(restaurantId);
        try {
            List<String> adminEmails = getAdminEmails();
            if (adminEmails.isEmpty()) {
                log.warn("NotificationService: No admin emails found for tenant {}", restaurantId);
                return;
            }

            boolean isAgotado = event.level() == StockLevel.AGOTADO;
            String subject = (isAgotado ? "🔴 ALERTA CRÍTICA: Stock Agotado — " : "⚠️ ALERTA DE STOCK: Stock Crítico — ") + event.supplyName();

            Map<String, Object> model = Map.of(
                    "title", isAgotado ? "Stock Agotado" : "Stock Crítico",
                    "subtitle", "Al Toque - Gestión de Inventario",
                    "greeting", "Estimado Administrador:",
                    "isError", isAgotado,
                    "isWarning", !isAgotado,
                    "alertTitle", isAgotado ? "🔴 CRÍTICO:" : "⚠️ ADVERTENCIA:",
                    "alertText", "El insumo '" + event.supplyName() + "' se encuentra en estado " + event.level() + ".",
                    "paragraphs", List.of(
                            "Se ha registrado una disminución de existencias para este insumo que requiere atención inmediata.",
                            "Por favor, revisa tus niveles de almacén y genera una orden de compra o reposición a la brevedad."
                    ),
                    "keyDetails", Map.of(
                            "Insumo", event.supplyName(),
                            "Stock Disponible", event.currentStock() + " " + (isAgotado ? "(Agotado)" : ""),
                            "Umbral Mínimo", event.threshold().toString(),
                            "Nivel de Alerta", event.level().toString()
                    )
            );

            for (String email : adminEmails) {
                emailNotificationChannel.send(email, subject, "email-template", model);
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Async
    public void processCashRegisterMismatchedAsync(Long restaurantId, CashRegisterMismatched event) {
        TenantContext.setCurrentTenantId(restaurantId);
        try {
            List<String> adminEmails = getAdminEmails();
            if (adminEmails.isEmpty()) return;

            BigDecimal difference = event.expectedAmount().subtract(event.declaredAmount()).abs().setScale(2, RoundingMode.HALF_UP);
            String subject = "⚠️ Descuadre de caja detectado — Caja #" + event.cashRegisterId();

            Map<String, Object> model = Map.of(
                    "title", "Descuadre de Caja",
                    "subtitle", "Al Toque - Finanzas",
                    "greeting", "Estimado Administrador:",
                    "isWarning", true,
                    "alertTitle", "⚠️ ALERTA:",
                    "alertText", "Se ha detectado un descuadre al cerrar el arqueo de la caja registradora #" + event.cashRegisterId() + ".",
                    "highlightLabel", "Monto del Descuadre",
                    "highlightValue", "S/ " + difference,
                    "paragraphs", List.of(
                            "El monto declarado por el cajero difiere del saldo esperado según los movimientos de caja registrados hoy."
                    ),
                    "keyDetails", Map.of(
                            "Caja ID", "#" + event.cashRegisterId(),
                            "Monto Esperado", "S/ " + event.expectedAmount().setScale(2, RoundingMode.HALF_UP),
                            "Monto Declarado", "S/ " + event.declaredAmount().setScale(2, RoundingMode.HALF_UP),
                            "Diferencia", "S/ " + difference
                    )
            );

            for (String email : adminEmails) {
                emailNotificationChannel.send(email, subject, "email-template", model);
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Async
    public void processForcedCloseByCutoffAsync(Long restaurantId, ForcedCloseByCutoff event) {
        TenantContext.setCurrentTenantId(restaurantId);
        try {
            List<String> adminEmails = getAdminEmails();
            if (adminEmails.isEmpty()) return;

            String subject = "🔒 Cierre automático de caja por corte — Caja #" + event.cashRegisterId();
            String formattedTime = event.closedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            Map<String, Object> model = Map.of(
                    "title", "Cierre Forzado de Caja",
                    "subtitle", "Al Toque - Procesos Operativos",
                    "greeting", "Estimado Administrador:",
                    "isWarning", true,
                    "alertTitle", "🔒 SISTEMA:",
                    "alertText", "La caja #" + event.cashRegisterId() + " fue cerrada automáticamente.",
                    "paragraphs", List.of(
                            "La caja se encontraba abierta habiendo transcurrido la hora de corte configurada.",
                            "El sistema procedió con un cierre forzado automático por seguridad."
                    ),
                    "keyDetails", Map.of(
                            "Caja ID", "#" + event.cashRegisterId(),
                            "Fecha de Cierre", formattedTime
                    )
            );

            for (String email : adminEmails) {
                emailNotificationChannel.send(email, subject, "email-template", model);
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Async
    public void processStockMismatchDetectedAsync(Long restaurantId, StockMismatchDetected event) {
        TenantContext.setCurrentTenantId(restaurantId);
        try {
            List<String> adminEmails = getAdminEmails();
            if (adminEmails.isEmpty()) return;

            String subject = "🚨 INVENTARIO NEGATIVO: Desalineación de stock — " + event.supplyName();

            Map<String, Object> model = Map.of(
                    "title", "Desalineación de Stock",
                    "subtitle", "Al Toque - Control de Almacén",
                    "greeting", "Estimado Administrador:",
                    "isError", true,
                    "alertTitle", "🚨 CRÍTICO:",
                    "alertText", "Se ha detectado una desalineación de inventario para '" + event.supplyName() + "'.",
                    "paragraphs", List.of(
                            "Se intentó descontar un plato que requiere este insumo pero el stock disponible era insuficiente, resultando en un stock negativo.",
                            "Por favor, realiza un conteo físico y registra un ajuste manual para corregir el inventario."
                    ),
                    "keyDetails", Map.of(
                            "Insumo ID", "#" + event.supplyId(),
                            "Insumo", event.supplyName(),
                            "Cantidad Requerida", event.requiredQuantity().toString(),
                            "Stock Anterior", event.availableStock().toString(),
                            "Stock Estimado", event.availableStock().subtract(event.requiredQuantity()).toString()
                    )
            );

            for (String email : adminEmails) {
                emailNotificationChannel.send(email, subject, "email-template", model);
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Async
    public void processItemCancelledAsync(Long restaurantId, ItemCancelledEvent event, String productName) {
        TenantContext.setCurrentTenantId(restaurantId);
        try {
            // Validar la preferencia de notificaciones operativas
            List<OperationalConfig> configs = operationalConfigRepository.findAll();
            if (!configs.isEmpty()) {
                OperationalConfig config = configs.get(0);
                if ("DAILY".equalsIgnoreCase(config.getAnnulmentNotificationPref())) {
                    log.info("NotificationService: Annulment preference is DAILY, skipping instant email.");
                    return;
                }
            }

            List<String> adminEmails = getAdminEmails();
            if (adminEmails.isEmpty()) return;

            String subject = "🚫 Plato Anulado — Comanda #" + event.orderId();

            Map<String, Object> model = Map.of(
                    "title", "Anulación de Plato",
                    "subtitle", "Al Toque - Auditoría de Ventas",
                    "greeting", "Estimado Administrador:",
                    "isWarning", true,
                    "alertTitle", "🚫 ANULACIÓN:",
                    "alertText", "Se anuló o eliminó un plato de la comanda #" + event.orderId() + ".",
                    "paragraphs", List.of(
                            "Se ha registrado una anulación de comandas activas en el salón.",
                            "Por favor, revisa el motivo especificado a continuación."
                    ),
                    "keyDetails", Map.of(
                            "Comanda ID", "#" + event.orderId(),
                            "Producto", productName != null ? productName : "ID #" + event.productId(),
                            "Cantidad", event.quantity().toString(),
                            "Motivo", event.cancellationReason(),
                            "Detalle", event.detail() != null ? event.detail() : "Ninguno",
                            "Anulado Por", event.cancelledBy()
                    )
            );

            for (String email : adminEmails) {
                emailNotificationChannel.send(email, subject, "email-template", model);
            }
        } finally {
            TenantContext.clear();
        }
    }

    private List<String> getAdminEmails() {
        return userRepository.findAll().stream()
                .filter(User::getActive)
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == com.pezbackend.iam.domain.model.valueobjects.Roles.ADMIN))
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();
    }
}
