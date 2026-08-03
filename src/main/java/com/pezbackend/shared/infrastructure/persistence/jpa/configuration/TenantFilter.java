package com.pezbackend.shared.infrastructure.persistence.jpa.configuration;

import com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.pezbackend.shared.infrastructure.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that extracts the tenant ID (restaurantId) from the JWT token
 * and sets it in the TenantContext and enables the Hibernate filter.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final BearerTokenService tokenService;

    @PersistenceContext
    private EntityManager entityManager;

    public TenantFilter(BearerTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = tokenService.getBearerTokenFrom(request);

        if (token != null && tokenService.validateToken(token)) {
            Long restaurantId = tokenService.getRestaurantIdFromToken(token);
            if (restaurantId != null) {
                TenantContext.setCurrentTenantId(restaurantId);
                
                // Habilitar el Hibernate Filter en la sesión actual
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("restaurantId", restaurantId);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpiar el contexto para evitar fugas de memoria o de datos entre hilos
            TenantContext.clear();
        }
    }
}
