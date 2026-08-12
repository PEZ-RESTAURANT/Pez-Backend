package com.pezbackend.iam.infrastructure.tokens.jwt;

import com.pezbackend.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;


public interface BearerTokenService extends TokenService {

    /**
     * This method is responsible for extracting the JWT token from the HTTP request.
     * @param token the HTTP request
     * @return String the JWT token
     */
    String getBearerTokenFrom(HttpServletRequest token);

    /**
     * Invalida un token registrándolo en la lista negra.
     * @param token el token JWT a revocar
     */
    void invalidateToken(String token);

    /**
     * Verifica si un token está registrado en la lista negra.
     * @param token el token JWT a verificar
     * @return true si el token fue revocado, false de lo contrario
     */
    boolean isTokenInvalidated(String token);

    /**
     * Elimina de la base de datos todos los tokens expirados en la lista negra.
     */
    void cleanExpiredTokens();

    /**
     * Obtiene la fecha de emisión (Issued At) del token.
     * @param token el token JWT
     * @return la fecha de emisión
     */
    java.util.Date getIssuedAtFromToken(String token);
}