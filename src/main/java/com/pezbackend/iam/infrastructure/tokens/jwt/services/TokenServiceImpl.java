package com.pezbackend.iam.infrastructure.tokens.jwt.services;

import com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Token service implementation for JWT tokens.
 * This class is responsible for generating and validating JWT tokens.
 * It uses the secret and expiration days from the application.properties file.
 */
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.BlacklistedTokenRepository;
import com.pezbackend.iam.domain.model.entities.BlacklistedToken;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.RestaurantRepository;

@Service
public class TokenServiceImpl implements BearerTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RestaurantRepository restaurantRepository;

    public TokenServiceImpl(
            BlacklistedTokenRepository blacklistedTokenRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.restaurantRepository = restaurantRepository;
    }

    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private static final String WORKSHOP_ID_CLAIM = "workshop_id";
    private static final String ROLE_CLAIM = "role";
    private static final String RESTAURANT_ID_CLAIM = "restaurantId";
    private static final String RESTAURANT_NAME_CLAIM = "restaurantName";
    private static final int TOKEN_BEGIN_INDEX = 7;

    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;


    /**
     * Generates a JWT token based on the user ID, user role.
     * @param userId The user ID (used as the subject)
     * @param userRole The user's role
     * @return The generated JWT token
     */
    @Override
    public String generateToken(Long userId, String userRole) {
        return generateToken(userId, userRole, null);
    }

    @Override
    public String generateToken(Long userId, String userRole, Long restaurantId) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();

        String restaurantName = null;
        if (restaurantId != null) {
            try {
                restaurantName = restaurantRepository.findById(restaurantId)
                        .map(com.pezbackend.tenancy.domain.model.aggregates.Restaurant::getName)
                        .orElse(null);
            } catch (Exception e) {
                LOGGER.warn("Could not load restaurant name for token claim: {}", e.getMessage());
            }
        }

        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim(ROLE_CLAIM, userRole)
                .claim(RESTAURANT_ID_CLAIM, restaurantId)
                .claim(RESTAURANT_NAME_CLAIM, restaurantName)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key);

        return builder.compact();
    }

    @Override
    public Long getRestaurantIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get(RESTAURANT_ID_CLAIM, Long.class));
    }


    @Override
    public Long getUserIdFromToken(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            LOGGER.error("Subject in token is not a valid Long ID: {}", subject);
            throw new SecurityException("Invalid user ID format in token subject.");
        }
    }

    @Override
    public Long getWorkshopIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get(WORKSHOP_ID_CLAIM, Long.class));
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            LOGGER.info("Token is valid");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Logs and re-throws all security-related token exceptions
            LOGGER.error("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenPresentIn(String authorizationParameter) {
        return StringUtils.hasText(authorizationParameter);
    }


    private boolean isBearerTokenIn(String authorizationParameter) {
        return authorizationParameter.startsWith(BEARER_TOKEN_PREFIX);
    }

    private String extractTokenFrom(String authorizationHeaderParameter) {
        return authorizationHeaderParameter.substring(TOKEN_BEGIN_INDEX);
    }

    private String getAuthorizationParameterFrom(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_PARAMETER_NAME);
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String parameter = getAuthorizationParameterFrom(request);

        if (isTokenPresentIn(parameter) && isBearerTokenIn(parameter)) return extractTokenFrom(parameter);
        return null;
    }

    @Override
    public void invalidateToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Date expirationDate = extractClaim(token, Claims::getExpiration);
            java.time.LocalDateTime expiresAt = java.time.LocalDateTime.ofInstant(
                    expirationDate.toInstant(),
                    java.time.ZoneId.systemDefault()
            );

            if (!blacklistedTokenRepository.existsByToken(token)) {
                blacklistedTokenRepository.save(new BlacklistedToken(token, expiresAt));
                LOGGER.info("Token añadido a la lista negra con expiración en: {}", expiresAt);
            }
        } catch (Exception e) {
            java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusDays(expirationDays);
            if (!blacklistedTokenRepository.existsByToken(token)) {
                blacklistedTokenRepository.save(new BlacklistedToken(token, expiresAt));
                LOGGER.warn("Token no pudo ser decodificado pero fue añadido a lista negra con expiración default: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean isTokenInvalidated(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void cleanExpiredTokens() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        blacklistedTokenRepository.deleteByExpiresAtBefore(now);
        LOGGER.info("Limpieza de tokens revocados expirados completada.");
    }

    @Override
    public java.util.Date getIssuedAtFromToken(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
}
