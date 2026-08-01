package com.pezbackend.shared.domain.model;

import java.time.LocalDateTime;

/**
 * Interfaz de marcador para todos los eventos de dominio de la aplicación.
 * <p>
 * Diseñada para soportar componentes públicos de Java Records (component-style accessors)
 * de forma nativa e inmutable, simplificando la publicación de eventos sin código repetitivo.
 * </p>
 */
public interface DomainEvent {

    /**
     * Obtiene el tipo o nombre único del evento de negocio (ej. "ItemCancelled").
     *
     * @return el tipo del evento
     */
    String eventType();

    /**
     * Obtiene el identificador del módulo/bounded context origen del evento (ej. "orders").
     *
     * @return el nombre del módulo origen
     */
    String module();

    /**
     * Obtiene el identificador único del usuario que provocó la acción (ej. "user_123").
     *
     * @return el identificador del usuario
     */
    String userId();

    /**
     * Obtiene opcionalmente el identificador del dispositivo físico/virtual que originó la petición.
     *
     * @return el identificador del dispositivo, o null si no aplica
     */
    default String deviceId() {
        return null;
    }

    /**
     * Obtiene la carga útil (payload) específica del evento. Representa los datos relevantes del evento.
     *
     * @return el objeto de carga útil libre del evento
     */
    Object payload();

    /**
     * Obtiene opcionalmente la justificación o motivo del evento de negocio.
     *
     * @return la razón del evento, o null si no aplica
     */
    default String reason() {
        return null;
    }

    /**
     * Obtiene la fecha y hora precisa en la que ocurrió el evento de dominio.
     *
     * @return la marca de tiempo del evento
     */
    default LocalDateTime timestamp() {
        return LocalDateTime.now();
    }
}
