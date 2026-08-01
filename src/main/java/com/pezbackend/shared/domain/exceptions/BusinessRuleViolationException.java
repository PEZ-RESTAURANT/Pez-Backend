package com.pezbackend.shared.domain.exceptions;

import java.util.Map;

/**
 * Excepción de dominio para indicar que se ha violado una regla de negocio explícita del sistema.
 * <p>
 * Esto generalmente se traduce en un código de estado HTTP 409 (Conflict).
 * </p>
 */
public class BusinessRuleViolationException extends DomainException {

    /**
     * Construye la excepción con un código de error único y un mensaje detallado.
     *
     * @param errorCode código de error único y estable (ej. "INSUFFICIENT_STOCK")
     * @param message   mensaje explicativo legible para el cliente
     */
    public BusinessRuleViolationException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Construye la excepción con un código de error único, un mensaje detallado y detalles adicionales.
     *
     * @param errorCode código de error único y estable (ej. "INSUFFICIENT_STOCK")
     * @param message   mensaje explicativo legible para el cliente
     * @param details   información estructurada de soporte
     */
    public BusinessRuleViolationException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
