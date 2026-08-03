package com.pezbackend.shared.domain.model.entities;

import com.pezbackend.shared.infrastructure.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * Mapped superclass for non-audited entities requiring tenant isolation.
 * Contains only the restaurant ID field and applies the tenant filter.
 */
@Getter
@Setter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "restaurantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public abstract class AbstractTenantBaseEntity {

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @PrePersist
    public void prePersist() {
        if (this.restaurantId == null) {
            Long current = TenantContext.getCurrentTenantId();
            this.restaurantId = (current != null) ? current : 1L;
        }
    }
}
