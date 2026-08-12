package com.pezbackend.iam.infrastructure.persistence.jpa.repositories;

import com.pezbackend.iam.domain.model.entities.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio JPA para gestionar la lista negra de tokens JWT revocados.
 */
@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    
    /**
     * Busca un token en la lista negra.
     * @param token el token a buscar
     * @return un Optional con el token encontrado o vacío
     */
    Optional<BlacklistedToken> findByToken(String token);

    /**
     * Determina si existe un token en la lista negra.
     * @param token el token a verificar
     * @return true si está en la lista negra, false de lo contrario
     */
    boolean existsByToken(String token);

    /**
     * Elimina todos los tokens que han expirado antes de la fecha dada.
     * @param now fecha de referencia
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
}
