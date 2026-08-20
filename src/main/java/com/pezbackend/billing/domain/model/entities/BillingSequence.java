package com.pezbackend.billing.domain.model.entities;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa la secuencia de numeración correlativa
 * para un tipo de comprobante en un restaurante.
 */
@Getter
@Setter
@Entity
@Table(name = "billing_sequences", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurant_id", "document_type"})
})
public class BillingSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "current_value", nullable = false)
    private Integer currentValue = 0;

    protected BillingSequence() {}

    public BillingSequence(Long restaurantId, DocumentType documentType, Integer currentValue) {
        this.restaurantId = restaurantId;
        this.documentType = documentType;
        this.currentValue = currentValue;
    }
}
