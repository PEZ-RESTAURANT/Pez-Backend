package com.pezbackend.shared.domain.exceptions;

/**
 * Excepción lanzada cuando hay un intento de acceder a recursos de otro tenant (restaurante).
 * Para evitar revelar la existencia del recurso a inquilinos no autorizados, esto se mapea a un HTTP 404 (Not Found).
 */
public class TenantMismatchException extends DomainException {
    public TenantMismatchException(String resourceName, Long resourceId) {
        super("RESOURCE_NOT_FOUND", String.format("%s with ID %d was not found.", resourceName, resourceId));
    }
}
