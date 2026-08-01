package com.pezbackend.catalog.domain.model.aggregates;

import com.pezbackend.catalog.domain.model.exceptions.InvalidProductCategoryException;
import com.pezbackend.catalog.domain.model.exceptions.InvalidProductNameException;
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

    @Column(name = "estimated_prep_time_minutes")
    private Integer estimatedPrepTimeMinutes;

    @Column(nullable = false)
    private boolean active = true;

    protected Product() {}

    public Product(String name, BigDecimal price, ProductCategory category) {
        validate(name, price, category);

        this.name = name;
        this.price = price;
        this.category = category;
        this.active = true;
        this.estimatedPrepTimeMinutes = null;
    }

    public Product(String name, BigDecimal price, ProductCategory category, Integer estimatedPrepTimeMinutes, Boolean active) {
        validate(name, price, category);

        this.name = name;
        this.price = price;
        this.category = category;
        this.estimatedPrepTimeMinutes = estimatedPrepTimeMinutes;
        this.active = active != null ? active : true;
    }

    public void update(String name, BigDecimal price, ProductCategory category, Integer estimatedPrepTimeMinutes, Boolean active) {
        if (name == null || name.isBlank())
            throw new InvalidProductNameException();

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidProductPriceException(price);

        if (category == null)
            throw new InvalidProductCategoryException();

        this.name = name;
        this.price = price;
        this.category = category;
        this.estimatedPrepTimeMinutes = estimatedPrepTimeMinutes;
        this.active = active != null ? active : true;
    }

    public void update(String name, BigDecimal price, ProductCategory category) {
        update(name, price, category, this.estimatedPrepTimeMinutes, this.active);
    }

    private void validate(String name, BigDecimal price, ProductCategory category) {
        if (name == null || name.isBlank())
            throw new InvalidProductNameException();

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidProductPriceException(price);

        if (category == null)
            throw new InvalidProductCategoryException();
    }
}