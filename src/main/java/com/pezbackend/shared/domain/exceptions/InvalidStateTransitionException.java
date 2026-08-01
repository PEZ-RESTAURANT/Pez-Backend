package com.pezbackend.shared.domain.exceptions;

import java.util.Map;

/**
 * Excepción de dominio para indicar un intento de transición de estado no válida en una máquina de estados.
 * <p>
 * Esto generalmente se traduce en un código de estado HTTP 409 (Conflict).
 * </p>
 */
public class InvalidStateTransitionException extends DomainException {

    /**
     * Construye la excepción con un código de error único y un mensaje detallado.
     *
     * @param errorCode código de error único y estable (ej. "INVALID_STATE_TRANSITION")
     * @param message   mensaje explicativo legible para el cliente
     */
    public InvalidStateTransitionException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Construye la excepción con un código de error único, un mensaje detallado y detalles adicionales.
     *
     * @param errorCode código de error único y estable (ej. "INVALID_STATE_TRANSITION")
     * @param message   mensaje explicativo legible para el cliente
     * @param details   información de soporte para la transición
     */
    public InvalidStateTransitionException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
