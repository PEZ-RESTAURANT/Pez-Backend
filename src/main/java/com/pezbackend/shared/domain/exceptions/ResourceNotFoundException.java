package com.pezbackend.shared.domain.exceptions;

import java.util.Map;

/**
 * Excepción de dominio para indicar que un recurso específico no existe en el sistema.
 * <p>
 * Esto generalmente se traduce en un código de estado HTTP 404 (Not Found).
 * </p>
 */
public class ResourceNotFoundException extends DomainException {

    /**
     * Construye la excepción con un código de error único y un mensaje detallado.
     *
     * @param errorCode código de error único y estable (ej. "TABLE_NOT_FOUND")
     * @param message   mensaje explicativo legible para el cliente
     */
    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Construye la excepción con un código de error único, un mensaje detallado y detalles adicionales.
     *
     * @param errorCode código de error único y estable (ej. "TABLE_NOT_FOUND")
     * @param message   mensaje explicativo legible para el cliente
     * @param details   información estructurada de soporte
     */
    public ResourceNotFoundException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
