package com.pezbackend.cashregister.application.internal.services;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories.CashRegisterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Tarea programada que ejecuta el cierre automático de cajas registradoras por corte de horario.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CashRegisterScheduler {

    private final CashRegisterRepository cashRegisterRepository;
    private final CashRegisterCommandService commandService;

    @Value("${app.cash-register.cutoff-hour:3}")
    private int cutoffHour;

    @Value("${app.cash-register.cutoff-minute:0}")
    private int cutoffMinute;

    /**
     * Tarea que se ejecuta periódicamente (cada minuto) para buscar cajas abiertas
     * cuyo horario de corte ya haya transcurrido.
     */
    @Scheduled(cron = "0 * * * * ?")
    public void closeExpiredCashRegisters() {
        Optional<CashRegister> openRegisterOpt = cashRegisterRepository.findByStatus(CashRegisterStatus.OPEN);
        if (openRegisterOpt.isEmpty()) {
            return;
        }

        CashRegister register = openRegisterOpt.get();
        LocalDateTime now = LocalDateTime.now();
        LocalTime cutoffTime = LocalTime.of(cutoffHour, cutoffMinute);
        LocalDateTime cutoffToday = LocalDateTime.of(now.toLocalDate(), cutoffTime);

        // Si la caja fue creada antes del corte de hoy y ya pasó la hora de corte de hoy:
        LocalDateTime openedAt = register.getCreatedAt() != null ? register.getCreatedAt() : now;

        if (now.isAfter(cutoffToday) && openedAt.isBefore(cutoffToday)) {
            log.info("Cerrando caja registradora ID: {} automáticamente por corte de horario (Cutoff: {})", register.getId(), cutoffToday);
            try {
                commandService.forceCloseCashRegister(register.getId());
            } catch (Exception e) {
                log.error("Error al forzar cierre automático de caja ID: {}", register.getId(), e);
            }
        }
    }
}
