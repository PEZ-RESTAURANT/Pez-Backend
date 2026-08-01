package com.pezbackend.iam.domain.model.valueobjects;

/**
 * Representa los valores posibles de anulación (override) de un permiso para una cuenta de usuario específica.
 */
public enum OverrideValue {
    /**
     * El permiso es explícitamente concedido al usuario, ignorando la configuración por defecto de su rol.
     */
    GRANTED,

    /**
     * El permiso es explícitamente revocado para el usuario, ignorando la configuración por defecto de su rol.
     */
    REVOKED
}
