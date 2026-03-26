package com.pezbackend.catalog.infrastructure.persistence.jpa.repositories;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryAndNameContainingIgnoreCase(ProductCategory category, String name);

    long countByCategory(ProductCategory category);

    boolean existsByNameIgnoreCase(String name);
}