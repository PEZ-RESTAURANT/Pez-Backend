package com.pezbackend.iam.infrastructure.authorization.sfs.aspects;

import com.pezbackend.iam.domain.services.PermissionResolutionService;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.shared.domain.exceptions.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Aspecto encargado de interceptar las llamadas a métodos anotados con {@link RequiresPermission}
 * y verificar la posesión efectiva del permiso granular a través de {@link PermissionResolutionService}.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequiresPermissionAspect {

    private final PermissionResolutionService permissionResolutionService;

    /**
     * Intercepta la ejecución antes de ingresar al método anotado con `@RequiresPermission`.
     *
     * @param joinPoint          punto de unión de AspectJ
     * @param requiresPermission la anotación con la definición del permiso requerido
     */
    @Before("@annotation(requiresPermission)")
    public void authorize(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        String permissionCode = requiresPermission.value();
        log.debug("AOP: Comprobando permiso requerido '{}' para la acción", permissionCode);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("AOP: Acceso denegado, el usuario no está autenticado");
            throw new PermissionDeniedException("UNAUTHORIZED", "Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();
        if ("anonymousUser".equals(principal) || !(principal instanceof UserDetailsImpl userDetails)) {
            log.warn("AOP: Acceso denegado, principal de seguridad no válido");
            throw new PermissionDeniedException("UNAUTHORIZED", "Detalles de usuario no válidos o sesión no iniciada");
        }

        Long userId = userDetails.getId();
        if (!permissionResolutionService.hasPermission(userId, permissionCode)) {
            log.warn("AOP: Usuario ID '{}' no cuenta con el permiso requerido '{}'", userId, permissionCode);
            throw new PermissionDeniedException(
                    "INSUFFICIENT_PERMISSIONS",
                    "No cuenta con el permiso requerido: " + permissionCode
            );
        }

        log.debug("AOP: Permiso '{}' verificado para usuario ID '{}'", permissionCode, userId);
    }
}
