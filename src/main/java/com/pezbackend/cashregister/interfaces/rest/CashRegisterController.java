package com.pezbackend.cashregister.interfaces.rest;

import com.pezbackend.cashregister.domain.model.commands.*;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.queries.GetCashRegisterByIdQuery;
import com.pezbackend.cashregister.domain.model.queries.GetCashRegistersByDateRangeQuery;
import com.pezbackend.cashregister.domain.model.queries.GetCurrentCashRegisterQuery;
import com.pezbackend.cashregister.domain.services.CashRegisterCommandService;
import com.pezbackend.cashregister.domain.services.CashRegisterQueryService;
import com.pezbackend.cashregister.interfaces.rest.resources.*;
import com.pezbackend.cashregister.interfaces.rest.transform.CashMovementResourceAssembler;
import com.pezbackend.cashregister.interfaces.rest.transform.CashRegisterResourceAssembler;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de cajas registradoras y arqueos.
 */
@RestController
@RequestMapping("/api/v1/cash-registers")
public class CashRegisterController {

    private final CashRegisterCommandService commandService;
    private final CashRegisterQueryService queryService;
    private final CashRegisterResourceAssembler assembler;

    public CashRegisterController(CashRegisterCommandService commandService,
                                   CashRegisterQueryService queryService,
                                   CashRegisterResourceAssembler assembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.assembler = assembler;
    }

    // Abrir caja
    @PostMapping("/open")
    @RequiresPermission("cashregister.open_close_shift")
    public ResponseEntity<Void> open(@RequestBody OpenCashRegisterResource resource) {
        commandService.handle(new OpenCashRegisterCommand(resource.openingBalance()));
        return ResponseEntity.ok().build();
    }

    // Cerrar caja manual estándar
    @PostMapping("/close")
    @RequiresPermission("cashregister.open_close_shift")
    public ResponseEntity<Void> closeCashRegister() {
        commandService.handle(new CloseCashRegisterCommand());
        return ResponseEntity.ok().build();
    }

    // Cerrar caja con declaración de efectivo
    @PostMapping("/{id}/close-with-declaration")
    @RequiresPermission("cashregister.open_close_shift")
    public ResponseEntity<Void> closeWithDeclaration(
            @PathVariable Long id,
            @RequestBody CloseCashRegisterWithDeclarationResource resource
    ) {
        commandService.handle(new CloseCashRegisterWithDeclarationCommand(id, resource.declaredAmount()));
        return ResponseEntity.ok().build();
    }

    // Agregar movimiento manual
    @PostMapping("/movements")
    @RequiresPermission("cashregister.register_movement")
    public ResponseEntity<Void> addMovement(@RequestBody AddCashMovementResource resource) {
        commandService.handle(new AddCashMovementCommand(
                resource.type(),
                resource.amount(),
                resource.reason(),
                resource.note()
        ));
        return ResponseEntity.ok().build();
    }

    // Obtener caja actual
    @GetMapping("/current")
    @RequiresPermission("cashregister.view")
    public ResponseEntity<CashRegisterResource> getCurrentCashRegister() {
        CashRegister cashRegister = queryService.handle(new GetCurrentCashRegisterQuery());
        return ResponseEntity.ok(assembler.toResource(cashRegister));
    }

    // Obtener caja por id
    @GetMapping("/{id}")
    @RequiresPermission("cashregister.view")
    public ResponseEntity<CashRegisterResource> getCashRegisterById(@PathVariable Long id) {
        CashRegister cashRegister = queryService.handle(new GetCashRegisterByIdQuery(id));
        return ResponseEntity.ok(assembler.toResource(cashRegister));
    }

    // Movimientos filtrados por tipo
    @GetMapping("/{id}/movements")
    @RequiresPermission("cashregister.view")
    public ResponseEntity<List<CashMovementResource>> getMovementsByType(
            @PathVariable Long id,
            @RequestParam(required = false) String type
    ) {
        List<CashMovement> movements;
        if (type != null) {
            movements = queryService.handle(new com.pezbackend.cashregister.domain.model.queries.GetMovementsByTypeQuery(
                    id,
                    com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType.valueOf(type)
            ));
        } else {
            CashRegister cashRegister = queryService.handle(new com.pezbackend.cashregister.domain.model.queries.GetCashRegisterByIdQuery(id));
            movements = cashRegister.getMovements();
        }

        return ResponseEntity.ok(
                movements.stream().map(CashMovementResourceAssembler::toResource).toList()
        );
    }

    // Listar cajas por rango de fechas
    @GetMapping
    @RequiresPermission("cashregister.view")
    public ResponseEntity<List<CashRegisterResource>> getCashRegisters(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null) start = startDate.atStartOfDay();
        if (endDate != null) end = endDate.atTime(23, 59, 59);

        List<CashRegister> cashRegisters = queryService.handle(new GetCashRegistersByDateRangeQuery(start, end));

        return ResponseEntity.ok(
                cashRegisters.stream()
                        .map(assembler::toResource)
                        .toList()
        );
    }
}