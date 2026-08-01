package com.pezbackend.iam.infrastructure.authorization.sfs.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para restringir el acceso a los métodos controladores REST
 * basándose en la resolución de permisos granulares del usuario.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    /**
     * El código identificador del permiso requerido (ej. "orders.cancel_item").
     *
     * @return el código de permiso
     */
    String value();
}
