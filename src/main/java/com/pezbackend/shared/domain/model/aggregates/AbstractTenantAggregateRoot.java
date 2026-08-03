package com.pezbackend.shared.domain.model.aggregates;

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
import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * Mapped superclass for aggregate roots requiring tenant isolation.
 * Automatically manages the restaurant ID field and applies the tenant filter.
 *
 * @param <T> the type of the aggregate root
 */
@Getter
@Setter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "restaurantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public abstract class AbstractTenantAggregateRoot<T extends AbstractAggregateRoot<T>> extends AuditableAbstractAggregateRoot<T> {

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
