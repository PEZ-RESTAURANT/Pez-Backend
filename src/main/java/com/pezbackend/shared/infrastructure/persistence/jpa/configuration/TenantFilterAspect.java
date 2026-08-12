package com.pezbackend.shared.infrastructure.persistence.jpa.configuration;

import com.pezbackend.shared.infrastructure.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Aspect that automatically manages the tenant filter on the current Hibernate Session
 * before executing any Repository, Service, or Controller method.
 * If a tenant context is active, it enables the filter; otherwise, it disables it.
 */
@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* org.springframework.data.repository.Repository+.*(..)) || " +
            "within(@org.springframework.stereotype.Service *) || " +
            "within(@org.springframework.web.bind.annotation.RestController *)")
    public void enableTenantFilter() {
        Long tenantId = TenantContext.getCurrentTenantId();
        try {
            Session session = entityManager.unwrap(Session.class);
            if (tenantId != null) {
                session.enableFilter("tenantFilter").setParameter("restaurantId", tenantId);
            } else {
                session.disableFilter("tenantFilter");
            }
        } catch (Exception e) {
            // Ignore if session cannot be retrieved or unwrapped
        }
    }
}
