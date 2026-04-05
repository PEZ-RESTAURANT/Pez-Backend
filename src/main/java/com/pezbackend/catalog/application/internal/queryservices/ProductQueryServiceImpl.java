package com.pezbackend.catalog.application.internal.queryservices;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.exceptions.ProductNotFoundException;
import com.pezbackend.catalog.domain.model.queries.*;
import com.pezbackend.catalog.domain.model.valueobjects.ProductCategory;
import com.pezbackend.catalog.domain.services.ProductQueryService;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

        for (var category : ProductCategory.values()) {
            long count = productRepository.countByCategory(category);
            result.put(category.name(), count);
        }

        return result;
    }
}