package com.pezbackend.billing.interfaces.rest;

import com.pezbackend.billing.domain.model.entities.PaymentMethodConfig;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.PaymentMethodConfigRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodConfigController {

    private final PaymentMethodConfigRepository repository;

    // Obtener métodos activos (usado por Caja)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentMethodConfig>> getActiveMethods() {
        return ResponseEntity.ok(repository.findAllByActiveTrue());
    }

    // Obtener todos los métodos (usado por Admin en configuración)
    @GetMapping
    @RequiresPermission("catalog.edit_payment_methods")
    public ResponseEntity<List<PaymentMethodConfig>> getAllMethods() {
        return ResponseEntity.ok(repository.findAll());
    }

    // Habilitar o deshabilitar
    @PutMapping("/{id}/toggle")
    @RequiresPermission("catalog.edit_payment_methods")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        PaymentMethodConfig config = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found"));
        config.setActive(active);
        repository.save(config);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @RequiresPermission("catalog.edit_payment_methods")
    public ResponseEntity<PaymentMethodConfig> createMethod(@RequestBody PaymentMethodConfig config) {
        return ResponseEntity.ok(repository.save(config));
    }
}
