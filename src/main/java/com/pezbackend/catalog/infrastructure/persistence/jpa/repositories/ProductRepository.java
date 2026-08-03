package com.pezbackend.catalog.infrastructure.persistence.jpa.repositories;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(Category category);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryAndNameContainingIgnoreCase(Category category, String name);

    long countByCategory(Category category);

    boolean existsByNameIgnoreCase(String name);
    
    boolean existsByCategoryId(Long categoryId);
}