package com.pezbackend.shared.domain.exceptions;

import java.util.Collections;
import java.util.Map;

/**
 * Excepción base abstracta para todos los errores de lógica de negocio o de dominio.
 * <p>
 * Proporciona un código de error único y estable (ej. "TABLE_NOT_FOUND") y un mapa opcional
 * con información adicional útil para depuración o para que los clientes manejen el error de forma programática.
 * </p>
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> details;

    /**
     * Construye una excepción de dominio con un código de error y un mensaje descriptivo.
     *
     * @param errorCode código de error único y estable para la identificación del problema en clientes
     * @param message   mensaje detallado legible por humanos
     */
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = Collections.emptyMap();
    }

    /**
     * Construye una excepción de dominio con un código de error, un mensaje descriptivo y detalles adicionales.
     *
     * @param errorCode código de error único y estable para la identificación del problema en clientes
     * @param message   mensaje detallado legible por humanos
     * @param details   mapa de detalles adicionales con información estructurada sobre el fallo
     */
    protected DomainException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? details : Collections.emptyMap();
    }

    /**
     * Obtiene el código de error único interno de la aplicación.
     *
     * @return el identificador de error estable
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Obtiene el mapa de detalles adicionales asociados al error.
     *
     * @return un mapa inmutable de detalles del error
     */
    public Map<String, Object> getDetails() {
        return details;
    }
}
