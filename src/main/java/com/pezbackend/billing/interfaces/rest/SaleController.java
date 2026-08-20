package com.pezbackend.billing.interfaces.rest;

import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.commands.CreateSaleCommand;
import com.pezbackend.billing.domain.model.queries.*;
import com.pezbackend.billing.domain.model.valueobjects.PaymentDetail;
import com.pezbackend.billing.domain.services.SaleCommandService;
import com.pezbackend.billing.domain.services.SaleQueryService;
import com.pezbackend.billing.interfaces.rest.resources.CreateSaleResource;
import com.pezbackend.billing.interfaces.rest.resources.RegisterPaymentsResource;
import com.pezbackend.billing.interfaces.rest.resources.SaleResource;
import com.pezbackend.billing.interfaces.rest.resources.VoidSaleResource;
import com.pezbackend.billing.interfaces.rest.transform.CreateSaleCommandFromResourceAssembler;
import com.pezbackend.billing.interfaces.rest.transform.SaleResourceFromEntityAssembler;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para el módulo de facturación y ventas.
 */
@RestController
@RequestMapping("/api/v1/sales")
public class SaleController {

    private final SaleCommandService commandService;
    private final SaleQueryService queryService;
    private final SaleRepository saleRepository;
    private final SaleResourceFromEntityAssembler assembler;

    public SaleController(SaleCommandService commandService, 
                          SaleQueryService queryService, 
                          SaleRepository saleRepository,
                          SaleResourceFromEntityAssembler assembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.saleRepository = saleRepository;
        this.assembler = assembler;
    }

    // Emitir comprobante
    @PostMapping
    @RequiresPermission("orders.issue_receipt")
    public ResponseEntity<Long> create(@RequestBody CreateSaleResource resource) {
        CreateSaleCommand command = CreateSaleCommandFromResourceAssembler.toCommandFromResource(resource);
        Long saleId = commandService.handle(command);
        return ResponseEntity.ok(saleId);
    }

    // Registrar pagos de una venta
    @PostMapping("/{id}/payments")
    @RequiresPermission("orders.issue_receipt")
    public ResponseEntity<Void> registerPayments(
            @PathVariable Long id,
            @RequestBody RegisterPaymentsResource resource
    ) {
        List<PaymentDetail> payments = resource.payments().stream()
                .map(p -> new PaymentDetail(p.method(), p.amount()))
                .toList();

        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        commandService.registerPayments(id, payments, executor);
        return ResponseEntity.ok().build();
    }

    // Anular una venta
    @PostMapping("/{id}/void")
    @RequiresPermission("billing.void_sale")
    public ResponseEntity<Void> voidSale(
            @PathVariable Long id,
            @RequestBody VoidSaleResource resource
    ) {
        String executor = SecurityContextHolder.getContext().getAuthentication().getName();
        commandService.voidSale(id, resource.voidedReason(), executor);
        return ResponseEntity.ok().build();
    }

    // Obtener todos
    @GetMapping
    @RequiresPermission("cashregister.view")
    public ResponseEntity<List<SaleResource>> getSales(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String customerDocumentNumber
    ) {
        List<Sale> sales;

        if (customerDocumentNumber != null) {
            sales = saleRepository.findByCustomerDocumentNumber(customerDocumentNumber);
        } else if (from == null && to == null) {
            sales = queryService.handle(new GetCurrentSalesQuery());
        } else {
            LocalDateTime startDate = LocalDate.parse(from).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(to).atTime(23, 59, 59);

            sales = queryService.handle(
                    new GetSalesBetweenDatesQuery(startDate, endDate)
            );
        }

        return ResponseEntity.ok(
                sales.stream()
                        .map(assembler::toResourceFromEntity)
                        .toList()
        );
    }

    // Obtener por id
    @GetMapping("/{id}")
    @RequiresPermission("cashregister.view")
    public ResponseEntity<SaleResource> getById(@PathVariable Long id) {
        Sale sale = queryService.handle(new GetSaleByIdQuery(id));
        return ResponseEntity.ok(assembler.toResourceFromEntity(sale));
    }

    // Obtener por tipo de documento
    @GetMapping("/document-type/{documentType}")
    @RequiresPermission("cashregister.view")
    public ResponseEntity<List<SaleResource>> getByDocumentType(@PathVariable String documentType) {
        List<Sale> sales = queryService.handle(new GetSalesByDocumentTypeQuery(
                Enum.valueOf(com.pezbackend.billing.domain.model.valueobjects.DocumentType.class, documentType)
        ));
        return ResponseEntity.ok(
                sales.stream()
                        .map(assembler::toResourceFromEntity)
                        .toList()
        );
    }

    // Obtener por medio de pago
    @GetMapping("/payment-method/{paymentMethod}")
    @RequiresPermission("cashregister.view")
    public ResponseEntity<List<SaleResource>> getByPaymentMethod(@PathVariable String paymentMethod) {
        List<Sale> sales = queryService.handle(new GetSalesByPaymentMethodQuery(
                Enum.valueOf(com.pezbackend.billing.domain.model.valueobjects.PaymentMethod.class, paymentMethod)
        ));
        return ResponseEntity.ok(
                sales.stream()
                        .map(assembler::toResourceFromEntity)
                        .toList()
        );
    }

    // Obtener por personal
    @GetMapping("/staff/{staffId}")
    @RequiresPermission("cashregister.view")
    public ResponseEntity<List<SaleResource>> getByStaff(@PathVariable Long staffId) {
        List<Sale> sales = queryService.handle(new GetSalesByStaffQuery(staffId));
        return ResponseEntity.ok(
                sales.stream()
                        .map(assembler::toResourceFromEntity)
                        .toList()
        );
    }
}