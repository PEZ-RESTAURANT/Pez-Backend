package com.pezbackend.billing.interfaces.rest;

import com.pezbackend.billing.domain.services.DniRucLookupService;
import com.pezbackend.billing.domain.services.LookupResult;
import com.pezbackend.shared.domain.model.AuditEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controlador REST para realizar la consulta externa de DNI o RUC.
 * Protegido bajo el permiso 'orders.issue_receipt' y con rate limiting incorporado de 20 peticiones/minuto por usuario.
 */
@RestController
@RequestMapping("/api/v1/lookup")
@Slf4j
public class LookupController {

    private final DniRucLookupService lookupService;
    private final AuditEventRepository auditEventRepository;
    private final ConcurrentHashMap<String, UserLimit> rateLimits = new ConcurrentHashMap<>();

    public LookupController(DniRucLookupService lookupService, AuditEventRepository auditEventRepository) {
        this.lookupService = lookupService;
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Realiza la consulta externa de un DNI (8 dígitos).
     */
    @GetMapping("/dni/{numero}")
    @RequiresPermission("orders.issue_receipt")
    public ResponseEntity<?> lookupDni(@PathVariable String numero) {
        String username = getAuthenticatedUsername();

        if (!checkRateLimit(username)) {
            log.warn("Rate limit excedido para el usuario {} al consultar DNI {}", username, numero);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Límite de consultas excedido (máximo 20 por minuto). Intente más tarde.");
        }

        LookupResult result = lookupService.lookupDni(numero);

        // Registrar auditoría sensible
        logLookupAudit(username, numero, "DNI", result.success());

        if (result.success()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se pudo obtener información para el DNI especificado.");
        }
    }

    /**
     * Realiza la consulta externa de un RUC (11 dígitos).
     */
    @GetMapping("/ruc/{numero}")
    @RequiresPermission("orders.issue_receipt")
    public ResponseEntity<?> lookupRuc(@PathVariable String numero) {
        String username = getAuthenticatedUsername();

        if (!checkRateLimit(username)) {
            log.warn("Rate limit excedido para el usuario {} al consultar RUC {}", username, numero);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Límite de consultas excedido (máximo 20 por minuto). Intente más tarde.");
        }

        LookupResult result = lookupService.lookupRuc(numero);

        // Registrar auditoría sensible
        logLookupAudit(username, numero, "RUC", result.success());

        if (result.success()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se pudo obtener información para el RUC especificado.");
        }
    }

    private String getAuthenticatedUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "anonymous";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Valida el límite de peticiones (20 consultas por minuto por usuario).
     */
    private boolean checkRateLimit(String username) {
        long now = System.currentTimeMillis();
        UserLimit limit = rateLimits.computeIfAbsent(username, k -> new UserLimit(now));

        synchronized (limit) {
            if (now - limit.windowStart > 60000) {
                limit.windowStart = now;
                limit.requests.set(1);
                return true;
            } else {
                int current = limit.requests.incrementAndGet();
                return current <= 20;
            }
        }
    }

    /**
     * Persiste de forma directa la auditoría de la consulta externa sin requerir transaccionalidad de negocio.
     */
    private void logLookupAudit(String username, String documentNumber, String type, boolean success) {
        try {
            AuditEvent auditEvent = new AuditEvent(
                    "DniRucLookupPerformed",
                    "BILLING",
                    username,
                    null,
                    Map.of("documentNumber", documentNumber, "type", type, "success", success),
                    "Consulta externa de documento",
                    LocalDateTime.now()
            );
            auditEvent.setRestaurantId(TenantContext.getCurrentTenantId());
            auditEventRepository.save(auditEvent);
        } catch (Exception e) {
            log.error("Error al registrar auditoría de lookup para {}: {}", documentNumber, e.getMessage(), e);
        }
    }

    private static class UserLimit {
        long windowStart;
        final AtomicInteger requests = new AtomicInteger(0);

        UserLimit(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
