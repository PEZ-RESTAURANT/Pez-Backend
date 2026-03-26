package com.pezbackend.catalog.domain.services;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.queries.*;

import java.util.List;
import java.util.Map;

public interface ProductQueryService {

    List<Product> handle(GetAllProductsQuery query);

    List<Product> handle(GetProductsByCategoryQuery query);

    List<Product> handle(SearchProductsQuery query);

    Product handle(GetProductByIdQuery query);

    Map<String, Long> handle(GetProductCountByCategoryQuery query);
}