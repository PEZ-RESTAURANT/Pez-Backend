package com.pezbackend.billing.interfaces.rest;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.queries.*;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.billing.domain.services.SaleQueryService;
import com.pezbackend.billing.interfaces.rest.resources.CreateSaleResource;
import com.pezbackend.billing.interfaces.rest.resources.SaleResource;
import com.pezbackend.billing.interfaces.rest.transform.CreateSaleCommandFromResourceAssembler;
import com.pezbackend.billing.interfaces.rest.transform.SaleResourceFromEntityAssembler;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.AuthorizeRoles;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@PreAuthorize(AuthorizeRoles.CASHIER_OR_ADMIN)
public class SaleController {

    private final SaleCommandService commandService;
    private final SaleQueryService queryService;

    public SaleController(SaleCommandService commandService, SaleQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    // 🔥 CREATE
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CreateSaleResource resource) {
        CreateSaleCommand command = CreateSaleCommandFromResourceAssembler.toCommandFromResource(resource);
        Long saleId = commandService.handle(command);
        return ResponseEntity.ok(saleId);
    }

    // 🔍 GET ALL
    @GetMapping
    public ResponseEntity<List<SaleResource>> getAll() {
        List<Sale> sales = queryService.handle(new GetAllSalesQuery());
        return ResponseEntity.ok(
                sales.stream()
                        .map(SaleResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    // 🔍 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SaleResource> getById(@PathVariable Long id) {
        Sale sale = queryService.handle(new GetSaleByIdQuery(id));
        return ResponseEntity.ok(SaleResourceFromEntityAssembler.toResourceFromEntity(sale));
    }

    // 🔍 GET BY DOCUMENT TYPE
    @GetMapping("/document-type/{documentType}")
    public ResponseEntity<List<SaleResource>> getByDocumentType(@PathVariable String documentType) {
        List<Sale> sales = queryService.handle(new GetSalesByDocumentTypeQuery(
                Enum.valueOf(com.pezbackend.billing.domain.model.valueobjects.DocumentType.class, documentType)
        ));
        return ResponseEntity.ok(
                sales.stream()
                        .map(SaleResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    // 🔍 GET BY PAYMENT METHOD
    @GetMapping("/payment-method/{paymentMethod}")
    public ResponseEntity<List<SaleResource>> getByPaymentMethod(@PathVariable String paymentMethod) {
        List<Sale> sales = queryService.handle(new GetSalesByPaymentMethodQuery(
                Enum.valueOf(com.pezbackend.billing.domain.model.valueobjects.PaymentMethod.class, paymentMethod)
        ));
        return ResponseEntity.ok(
                sales.stream()
                        .map(SaleResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }

    // 🔍 GET BY STAFF
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<SaleResource>> getByStaff(@PathVariable Long staffId) {
        List<Sale> sales = queryService.handle(new GetSalesByStaffQuery(staffId));
        return ResponseEntity.ok(
                sales.stream()
                        .map(SaleResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()
        );
    }
}