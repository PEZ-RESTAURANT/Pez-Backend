package com.pezbackend.shared.infrastructure.persistence.jpa.configuration;

import com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.pezbackend.shared.infrastructure.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that extracts the tenant ID (restaurantId) from the JWT token
 * or the active SecurityContext authentication principal, and registers
 * it in the TenantContext.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final BearerTokenService tokenService;

    public TenantFilter(BearerTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Long restaurantId = null;

        // 1. Try to extract from Bearer token
        String token = tokenService.getBearerTokenFrom(request);
        if (token != null && tokenService.validateToken(token)) {
            restaurantId = tokenService.getRestaurantIdFromToken(token);
        }

        // 2. Fallback to SecurityContext principal (e.g. for mocked user request tests)
        if (restaurantId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
                restaurantId = userDetails.getRestaurantId();
            }
        }

        if (restaurantId != null) {
            TenantContext.setCurrentTenantId(restaurantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean context to prevent thread-local leakage
            TenantContext.clear();
        }
    }
}
