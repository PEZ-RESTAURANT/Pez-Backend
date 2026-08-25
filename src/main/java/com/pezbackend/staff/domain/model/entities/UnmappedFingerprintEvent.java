package com.pezbackend.staff.domain.model.entities;

import org.hibernate.annotations.Filter;
import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
2:  * Entidad JPA que representa un intento de marcaje por huella dactilar de un ID de usuario no mapeado.
3:  */
@Entity
@Table(name = "unmapped_fingerprint_events")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "restaurant_id = :restaurantId")
public class UnmappedFingerprintEvent extends AbstractTenantEntity {


    @NotNull
    @Column(name = "device_serial_number", nullable = false)
    private String deviceSerialNumber;

    @NotNull
    @Column(name = "device_user_id", nullable = false)
    private Integer deviceUserId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Constructor requerido por JPA.
     */
    protected UnmappedFingerprintEvent() {}

    public UnmappedFingerprintEvent(String deviceSerialNumber, Integer deviceUserId, LocalDateTime timestamp) {
        this.deviceSerialNumber = deviceSerialNumber;
        this.deviceUserId = deviceUserId;
        this.timestamp = timestamp;
    }
}
