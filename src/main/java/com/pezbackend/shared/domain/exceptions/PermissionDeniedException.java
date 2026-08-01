package com.pezbackend.shared.domain.exceptions;

import java.util.Map;

/**
 * Excepción de dominio para indicar que un usuario no cuenta con los permisos necesarios para realizar una acción.
 * <p>
 * Esto generalmente se traduce en un código de estado HTTP 403 (Forbidden).
 * </p>
 */
public class PermissionDeniedException extends DomainException {

    /**
     * Construye la excepción con un código de error único y un mensaje detallado.
     *
     * @param errorCode código de error único y estable (ej. "INSUFFICIENT_PERMISSIONS")
     * @param message   mensaje explicativo legible para el cliente
     */
    public PermissionDeniedException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Construye la excepción con un código de error único, un mensaje detallado y detalles adicionales.
     *
     * @param errorCode código de error único y estable (ej. "INSUFFICIENT_PERMISSIONS")
     * @param message   mensaje explicativo legible para el cliente
     * @param details   información estructurada de soporte
     */
    public PermissionDeniedException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
