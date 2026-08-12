package com.pezbackend.iam.infrastructure.tokens.jwt.services;

import com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler encargado de realizar la limpieza periódica de tokens expirados de la lista negra.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final BearerTokenService tokenService;
    private final com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Limpia los tokens expirados de la base de datos diariamente a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredTokens() {
        log.info("Iniciando tarea programada: Limpieza de tokens JWT revocados expirados...");
        try {
            tokenService.cleanExpiredTokens();
        } catch (Exception e) {
            log.error("Error durante la limpieza de tokens revocados: {}", e.getMessage(), e);
        }

        log.info("Iniciando tarea programada: Limpieza de tokens de restablecimiento de contraseña expirados...");
        try {
            passwordResetTokenRepository.deleteByExpiresAtBefore(java.time.LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error durante la limpieza de tokens de restablecimiento: {}", e.getMessage(), e);
        }
    }
}
