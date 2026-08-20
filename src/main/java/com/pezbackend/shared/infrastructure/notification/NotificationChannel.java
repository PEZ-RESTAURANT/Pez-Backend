package com.pezbackend.shared.infrastructure.notification;

import java.util.Map;

/**
 * Interfaz genérica para definir canales de envío de notificaciones.
 */
public interface NotificationChannel {

    /**
     * Envía una notificación usando una plantilla.
     *
     * @param recipient     destinatario
     * @param subject       asunto
     * @param templateName  nombre de la plantilla HTML
     * @param templateModel datos para rellenar la plantilla
     */
    void send(String recipient, String subject, String templateName, Map<String, Object> templateModel);
}
