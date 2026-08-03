package com.pezbackend.interfaces.rest.exceptionhandling;

import com.pezbackend.shared.domain.exceptions.*;
import com.pezbackend.shared.domain.model.exceptions.*;
import com.pezbackend.shared.interfaces.rest.exceptionhandling.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de asesoramiento global para interceptar y manejar excepciones lanzadas por la aplicación.
 * <p>
 * Estandariza la respuesta de error de cara al cliente garantizando que no se expongan stack traces sensibles
 * y proporciona información útil como códigos de error de negocio internos.
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones lanzadas por recursos que no existen en el sistema.
     *
     * @param ex      la excepción {@link ResourceNotFoundException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 404 (Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Recurso no encontrado: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                404,
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(404).body(error);
    }

    /**
     * Maneja los intentos de acceso cross-tenant no autorizados mapeándolos a HTTP 404 (Not Found).
     *
     * @param ex      la excepción {@link TenantMismatchException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 404 (Not Found)
     */
    @ExceptionHandler(TenantMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTenantMismatchException(
            TenantMismatchException ex,
            HttpServletRequest request
    ) {
        log.warn("⚠️ [CROSS-TENANT] Intento de acceso cross-tenant detectado: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                404,
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(404).body(error);
    }

    /**
     * Maneja las excepciones originadas por violaciones a reglas de negocio del sistema.
     *
     * @param ex      la excepción {@link BusinessRuleViolationException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 409 (Conflict)
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolationException(
            BusinessRuleViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Violación de regla de negocio: {} - Código: {} - URI: {}", ex.getMessage(), ex.getErrorCode(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                409,
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(409).body(error);
    }

    /**
     * Maneja las excepciones por denegación de permisos de negocio o de sistema.
     *
     * @param ex      la excepción {@link PermissionDeniedException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 403 (Forbidden)
     */
    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDeniedException(
            PermissionDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Permiso denegado: {} - Código: {} - URI: {}", ex.getMessage(), ex.getErrorCode(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                403,
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(403).body(error);
    }

    /**
     * Maneja las excepciones por intentos de transición de estado no válidos en máquinas de estados de dominio.
     *
     * @param ex      la excepción {@link InvalidStateTransitionException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 409 (Conflict)
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(
            InvalidStateTransitionException ex,
            HttpServletRequest request
    ) {
        log.warn("Transición de estado no válida: {} - Código: {} - URI: {}", ex.getMessage(), ex.getErrorCode(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                409,
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(409).body(error);
    }

    /**
     * Maneja las excepciones de validación de Spring Validation (Bean Validation).
     *
     * @param ex      la excepción {@link MethodArgumentNotValidException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 400 (Bad Request) y detalles de los campos que fallaron
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Fallo de validación de entrada en URI: {}. Detalles: {}", request.getRequestURI(), details);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                400,
                "VALIDATION_ERROR",
                "Fallo en la validación de los datos de entrada",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(400).body(error);
    }

    // --- Adaptadores para excepciones del legado / ya existentes en el proyecto ---

    /**
     * Maneja NotFoundException legada.
     *
     * @param ex      la excepción {@link NotFoundException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 404 (Not Found)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Recurso legado no encontrado: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                404,
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(404).body(error);
    }

    /**
     * Maneja BadRequestException legada.
     *
     * @param ex      la excepción {@link BadRequestException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 400 (Bad Request)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        log.warn("Petición incorrecta: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                400,
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(400).body(error);
    }

    /**
     * Maneja BusinessRuleException legada.
     *
     * @param ex      la excepción {@link BusinessRuleException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 422 (Unprocessable Entity)
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleException ex,
            HttpServletRequest request
    ) {
        log.warn("Violación de regla de negocio (legada): {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                422,
                "BUSINESS_RULE_VIOLATION",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(422).body(error);
    }

    /**
     * Maneja UnauthorizedException legada.
     *
     * @param ex      la excepción {@link UnauthorizedException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 401 (Unauthorized)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        log.warn("No autorizado: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                401,
                "UNAUTHORIZED",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(401).body(error);
    }

    /**
     * Maneja ForbiddenException legada.
     *
     * @param ex      la excepción {@link ForbiddenException} capturada
     * @param request la solicitud HTTP actual
     * @return una respuesta de error con código HTTP 403 (Forbidden)
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        log.warn("Prohibido: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                403,
                "FORBIDDEN",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(403).body(error);
    }

    /**
     * Manejador de fallback para cualquier excepción no mapeada de manera explícita.
     *
     * @param ex      la excepción {@link Exception} genérica
     * @param request la solicitud HTTP actual
     * @return una respuesta genérica con código HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Error no manejado al procesar la petición en URI: {}", request.getRequestURI(), ex);
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                500,
                "INTERNAL_SERVER_ERROR",
                "Ocurrió un error inesperado en el servidor",
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(500).body(error);
    }
}