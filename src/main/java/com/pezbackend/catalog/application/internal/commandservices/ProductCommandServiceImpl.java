package com.pezbackend.catalog.application.internal.commandservices;

import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.commands.CreateProductCommand;
import com.pezbackend.catalog.domain.model.commands.UpdateProductCommand;
import com.pezbackend.catalog.domain.model.commands.DeleteProductCommand;
import com.pezbackend.catalog.domain.model.exceptions.ProductAlreadyExistsException;
import com.pezbackend.catalog.domain.model.exceptions.ProductNotFoundException;
import com.pezbackend.catalog.domain.services.ProductCommandService;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;

    public ProductCommandServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Long handle(CreateProductCommand command) {

        if (productRepository.existsByNameIgnoreCase(command.name())) {
            throw new ProductAlreadyExistsException(command.name());
        }

        Product product = new Product(
                command.name(),
                command.price(),
                command.category()
        );

        productRepository.save(product);

        return product.getId();
    }

    @Override
    public void handle(UpdateProductCommand command) {

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.update(
                command.name(),
                command.price(),
                command.category()
        );

        productRepository.save(product);
    }

    @Override
    public void handle(DeleteProductCommand command) {

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        productRepository.delete(product);
    }
}