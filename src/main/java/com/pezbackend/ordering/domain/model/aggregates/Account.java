package com.pezbackend.ordering.domain.model.aggregates;

import com.pezbackend.ordering.domain.model.entities.AccountItem;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;

import java.util.ArrayList;
import java.util.List;

// TODO Fase 5: Eliminar esta clase/interfaz cuando el módulo billing se integre con com.pezbackend.orders
public class Account {
    private Long id;
    private String name;
    private Long staffId;
    private String customerName;
    private String customerDni;
    private String customerRuc;
    private AccountStatus status;
    private List<AccountItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerDni() {
        return customerDni;
    }

    public void setCustomerDni(String customerDni) {
        this.customerDni = customerDni;
    }

    public String getCustomerRuc() {
        return customerRuc;
    }

    public void setCustomerRuc(String customerRuc) {
        this.customerRuc = customerRuc;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public List<AccountItem> getItems() {
        return items;
    }

    public void setItems(List<AccountItem> items) {
        this.items = items;
    }
}