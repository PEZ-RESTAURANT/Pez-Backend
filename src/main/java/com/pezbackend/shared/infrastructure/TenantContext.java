package com.pezbackend.shared.infrastructure;

/**
 * Context holder for the current tenant's restaurant ID.
 * Uses a ThreadLocal to store the tenant ID associated with the current thread of execution.
 */
public class TenantContext {

    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();

    /**
     * Set the current tenant ID.
     *
     * @param tenantId the tenant ID to set
     */
    public static void setCurrentTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }

    /**
     * Get the current tenant ID.
     *
     * @return the current tenant ID, or null if not set
     */
    public static Long getCurrentTenantId() {
        return currentTenantId.get();
    }

    /**
     * Clear the current tenant ID from the ThreadLocal.
     */
    public static void clear() {
        currentTenantId.remove();
    }
}
