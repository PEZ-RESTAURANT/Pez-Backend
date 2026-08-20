package com.pezbackend.loyalty.application.internal.services;

import com.pezbackend.loyalty.domain.model.aggregates.Customer;
import com.pezbackend.loyalty.infrastructure.persistence.jpa.repositories.CustomerRepository;
import com.pezbackend.shared.infrastructure.TenantContext;
import com.pezbackend.shared.infrastructure.notification.EmailNotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tarea programada diaria que envía felicitaciones de cumpleaños automatizadas a los clientes afiliados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayScheduler {

    private final CustomerRepository customerRepository;
    private final EmailNotificationChannel emailNotificationChannel;

    // Ejecuta todos los días a las 09:00 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendBirthdayGreetings() {
        log.info("BirthdayScheduler: Starting daily birthday check...");
        
        // Deshabilitar temporalmente el filtro de inquilino para recuperar todos los clientes del sistema
        TenantContext.clear();
        List<Customer> customers = customerRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Customer c : customers) {
            if (c.getBirthday() != null && c.isDataConsentAccepted() && c.getEmail() != null && !c.getEmail().isBlank()) {
                LocalDate bday = c.getBirthday();
                if (bday.getMonth() == today.getMonth() && bday.getDayOfMonth() == today.getDayOfMonth()) {
                    Long tenantId = c.getRestaurantId();
                    if (tenantId == null) tenantId = 1L;

                    log.info("BirthdayScheduler: Customer {} is having a birthday today! Sending email under tenant ID: {}", c.getFullName(), tenantId);
                    
                    TenantContext.setCurrentTenantId(tenantId);
                    try {
                        String subject = "🎉 ¡Feliz Cumpleaños de parte de Al Toque!";
                        Map<String, Object> model = Map.of(
                            "title", "¡Feliz Cumpleaños!",
                            "subtitle", "Al Toque - Club de Fidelización",
                            "greeting", "¡Hola, " + c.getFullName() + "!",
                            "isSuccess", true,
                            "paragraphs", List.of(
                                "Hoy es tu día especial, y todo el equipo de Al Toque quiere desearte un muy feliz cumpleaños.",
                                "Queremos agradecerte por ser un cliente afiliado muy valioso para nosotros.",
                                "¡Pasa por nuestro local el día de hoy y reclama un postre o bebida especial de cortesía presentando este correo!"
                            )
                        );
                        emailNotificationChannel.send(c.getEmail(), subject, "email-template", model);
                    } catch (Exception e) {
                        log.error("BirthdayScheduler: Failed to send birthday greeting to {}", c.getEmail(), e);
                    } finally {
                        TenantContext.clear();
                    }
                }
            }
        }
    }
}
