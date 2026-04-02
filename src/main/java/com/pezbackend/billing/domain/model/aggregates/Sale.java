package com.pezbackend.billing.domain.model.aggregates;

import com.pezbackend.billing.domain.model.entities.SaleDetail;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Sale extends AuditableAbstractAggregateRoot<Sale> {

    private Long staffId;

    private String name;

    private String customerName;
    private String customerDni;
    private String customerRuc;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @NotNull
    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();

    protected Sale() {}

    public Sale(String name,
                Long staffId,
                String customerName,
                String customerDni,
                String customerRuc,
                DocumentType documentType,
                PaymentMethod paymentMethod) {
        this.name = name;
        this.staffId = staffId;
        this.customerName = customerName;
        this.customerDni = customerDni;
        this.customerRuc = customerRuc;
        this.documentType = documentType;
        this.paymentMethod = paymentMethod;
        this.total = BigDecimal.ZERO;
        this.details = new ArrayList<>();
    }

    // DOMAIN METHODS
    public void addDetail(String productName, BigDecimal unitPrice, int quantity, String note) {
        SaleDetail detail = new SaleDetail(this, productName, unitPrice, quantity, note);
        this.details.add(detail);
        recalcTotal();
    }

    private void recalcTotal() {
        this.total = details.stream()
                .map(SaleDetail::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}