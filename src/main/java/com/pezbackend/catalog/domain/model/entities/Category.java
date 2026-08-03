package com.pezbackend.catalog.domain.model.entities;

import com.pezbackend.shared.domain.model.entities.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category extends AbstractTenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    protected Category() {}

    public Category(String name) {
        this.name = name;
    }
}
