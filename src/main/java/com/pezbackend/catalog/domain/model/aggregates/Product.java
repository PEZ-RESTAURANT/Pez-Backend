package com.pezbackend.catalog.domain.model.aggregates;

import com.pezbackend.catalog.domain.model.exceptions.InvalidProductPriceException;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import com.pezbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class Product extends AuditableAbstractAggregateRoot<Product> {

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @NotNull
    @Column(nullable = false)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    protected Product() {}

    public Product(String name, BigDecimal price, ProductCategory category) {
        validate(name, price, category);

        this.name = name;
        this.price = price;
        this.category = category;
    }

    public void update(String name, BigDecimal price, ProductCategory category) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) <= 0)
                throw new InvalidProductPriceException(price);
            this.price = price;
        }

        if (category != null) {
            this.category = category;
        }
    }

    private void validate(String name, BigDecimal price, ProductCategory category) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Product name cannot be empty");

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidProductPriceException(price);

        if (category == null)
            throw new IllegalArgumentException("Category cannot be null");
    }
}