package com.pezbackend.catalog.application.internal.queryservices;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.Category;
import com.pezbackend.catalog.domain.model.exceptions.ProductNotFoundException;
import com.pezbackend.catalog.domain.model.queries.*;
import com.pezbackend.catalog.domain.services.ProductQueryService;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Product> handle(GetAllProductsQuery query) {
        return productRepository.findAll();
    }

    @Override
    public List<Product> handle(GetProductsByCategoryQuery query) {
        return productRepository.findByCategory(query.category());
    }

    @Override
    public List<Product> handle(SearchProductsQuery query) {

        if (query.category() != null && query.name() != null) {
            return productRepository.findByCategoryAndNameContainingIgnoreCase(
                    query.category(),
                    query.name()
            );
        }

        if (query.category() != null) {
            return productRepository.findByCategory(query.category());
        }

        if (query.name() != null) {
            return productRepository.findByNameContainingIgnoreCase(query.name());
        }

        return productRepository.findAll();
    }

    @Override
    public Product handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId())
                .orElseThrow(() -> new ProductNotFoundException(query.productId()));
    }

    @Override
    public Map<String, Long> handle(GetProductCountByCategoryQuery query) {

        Map<String, Long> result = new HashMap<>();

        List<Category> categories = categoryRepository.findAll();
        for (var category : categories) {
            long count = productRepository.countByCategory(category);
            result.put(category.getName(), count);
        }

        return result;
    }
}