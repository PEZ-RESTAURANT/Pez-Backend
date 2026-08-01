package com.pezbackend.ordering.domain.model.entities;

import java.math.BigDecimal;

// TODO Fase 5: Eliminar esta clase/interfaz cuando el módulo billing se integre con com.pezbackend.orders
public class AccountItem {
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private String note;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}