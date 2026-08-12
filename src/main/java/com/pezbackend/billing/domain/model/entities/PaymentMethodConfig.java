package com.pezbackend.billing.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_method_configs")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class PaymentMethodConfig extends AbstractTenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 50)
    private PaymentMethod type;

    @Column(nullable = false)
    private boolean active = true;

    protected PaymentMethodConfig() {}

    public PaymentMethodConfig(String name, PaymentMethod type, boolean active) {
        this.name = name;
        this.type = type;
        this.active = active;
    }
}