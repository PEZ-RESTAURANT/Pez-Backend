package com.pezbackend.billing.domain.model.aggregates;

import com.pezbackend.billing.domain.model.entities.SaleDetail;
import com.pezbackend.billing.domain.model.entities.SalePayment;
import com.pezbackend.billing.domain.model.exceptions.EmptySaleException;
import com.pezbackend.billing.domain.model.exceptions.PaymentMismatchException;
import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.billing.domain.model.valueobjects.PaymentMethod;
import com.pezbackend.billing.domain.model.valueobjects.SaleStatus;
import com.pezbackend.shared.domain.model.aggregates.AbstractTenantAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de Agregado Raíz (Aggregate Root) que representa un documento de venta/facturación emitido.
 */
@Getter
@Setter
@Entity
public class Sale extends AbstractTenantAggregateRoot<Sale> {

    private Long staffId;
    private String name;

    private String customerName;
    private String customerDocumentNumber;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    private Long orderId;

    private BigDecimal total;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SalePayment> payments = new ArrayList<>();

    /**
     * Constructor requerido por la especificación de JPA. No debe ser utilizado directamente.
     */
    protected Sale() {}

    /**
     * Construye un comprobante de venta en estado inicial ISSUED_UNPAID.
     *
     * @param name                   identificador/etiqueta de la venta (ej: "Mesa 5")
     * @param staffId                ID del personal emisor
     * @param customerName           nombre del cliente
     * @param customerDocumentNumber número de documento (DNI o RUC) del cliente
     * @param documentType           tipo de comprobante
     * @param orderId                ID de la comanda de origen
     */
    public Sale(String name,
                Long staffId,
                String customerName,
                String customerDocumentNumber,
                DocumentType documentType,
                Long orderId) {
        this.name = name;
        this.staffId = staffId;
        this.customerName = customerName;
        this.customerDocumentNumber = customerDocumentNumber;
        this.documentType = documentType;
        this.orderId = orderId;
        this.saleStatus = SaleStatus.ISSUED_UNPAID;
        this.total = BigDecimal.ZERO;
        this.details = new ArrayList<>();
        this.payments = new ArrayList<>();
    }

    /**
     * Agrega un detalle de ítem vendido.
     */
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

    /**
     * Registra un pago en efectivo u otro medio.
     */
    public void addPayment(PaymentMethod method, BigDecimal amount) {
        SalePayment payment = new SalePayment(this, method, amount);
        this.payments.add(payment);
    }

    /**
     * Valida que la suma de los pagos realizados coincida exactamente con el total de la venta.
     */
    public void validatePayments() {
        BigDecimal paymentTotal = payments.stream()
                .map(SalePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (payments.isEmpty())
            throw new EmptySaleException();

        if (paymentTotal.compareTo(this.total) != 0)
            throw new PaymentMismatchException();
    }
}