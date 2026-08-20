package com.pezbackend.iam.infrastructure.authorization.sfs.pipeline;

import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bearer Authorization Request Filter
 * <p>
 * This filter is responsible for processing the JWT token from the request header
 * and setting the authentication in the security context if the token is valid.
 * </p>
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);

    private final BearerTokenService tokenService;
    private final UserDetailsServiceExtension userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsServiceExtension userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        System.out.println("➡️ Request entrante a: " + path);

        // 1️⃣ Permitir preflight requests (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 2️⃣ Endpoints públicos (sin token requerido)
        boolean isPublicInviteGet = "GET".equalsIgnoreCase(request.getMethod()) && path.matches("/api/v1/staff/invites/[a-zA-Z0-9_-]+");
        boolean isPublicInviteAccept = "POST".equalsIgnoreCase(request.getMethod()) && path.matches("/api/v1/staff/invites/[a-zA-Z0-9_-]+/accept");

        if (path.contains("/api/v1/users/signup") || path.contains("/api/v1/users/signin") || path.contains("/api/v1/restaurants/onboarding") || path.contains("/ws") || path.contains("/api/v1/auth/") || isPublicInviteGet || isPublicInviteAccept) {
            System.out.println("🟢 Ruta pública detectada (" + path + "), omitiendo validación JWT");
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Validación normal del token
        String token = tokenService.getBearerTokenFrom(request);

        if (StringUtils.hasText(token)) {
            try {
                if (tokenService.isTokenInvalidated(token)) {
                    LOGGER.warn("Token revocado utilizado en petición a: {}", path);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token has been revoked.");
                    return;
                }

                if (tokenService.validateToken(token)) {
                    Long userId = tokenService.getUserIdFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserById(userId);

                    if (!userDetails.isEnabled()) {
                        LOGGER.warn("Petición rechazada: Cuenta desactivada para el usuario ID {}", userId);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("User account is deactivated.");
                        return;
                    }

                    if (userDetails instanceof UserDetailsImpl) {
                        UserDetailsImpl customUserDetails = (UserDetailsImpl) userDetails;
                        if (customUserDetails.getPasswordChangedAt() != null) {
                            java.util.Date issuedAt = tokenService.getIssuedAtFromToken(token);
                            java.time.LocalDateTime issuedAtLdt = java.time.LocalDateTime.ofInstant(
                                    issuedAt.toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );
                            if (issuedAtLdt.isBefore(customUserDetails.getPasswordChangedAt())) {
                                LOGGER.warn("Token revocado debido a cambio de contraseña posterior para el usuario ID {}", userId);
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("Token has been revoked due to password change.");
                                return;
                            }
                        }
                    }

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    var authenticationToken = UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request);
                    context.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(context);
                }
            } catch (Exception e) {
                LOGGER.error("Error procesando autenticación en BearerAuthorizationRequestFilter: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
} 