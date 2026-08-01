package com.pezbackend.shared.interfaces.rest.exceptionhandling;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representa el formato único de respuesta para errores de la API HTTP.
 *
 * @param timestamp hora exacta en la que se generó la respuesta de error
 * @param status    código de estado HTTP (ej. 404, 400, 500)
 * @param errorCode código de error interno estable del negocio (ej. "TABLE_NOT_FOUND")
 * @param message   mensaje explicativo legible por humanos
 * @param path      ruta URI de la solicitud que falló (ej. "/api/v1/tables/12")
 * @param details   detalles adicionales específicos sobre el error, por ejemplo, fallos de campos de validación
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        Map<String, Object> details
) {}
