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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-registers")
public class CashRegisterController {

    private final CashRegisterCommandService commandService;
    private final CashRegisterQueryService queryService;

    public CashRegisterController(CashRegisterCommandService commandService,
                                  CashRegisterQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    // Abrir caja
    @PostMapping("/open")
    public ResponseEntity<Void> open(@RequestBody OpenCashRegisterResource resource) {
        commandService.handle(new OpenCashRegisterCommand(resource.openingBalance()));
        return ResponseEntity.ok().build();
    }

    // Cerrar caja
    @PostMapping("/close")
    public ResponseEntity<Void> closeCashRegister() {
        commandService.handle(new CloseCashRegisterCommand());
        return ResponseEntity.ok().build();
    }

    // Agregar movimiento manual
    @PostMapping("/movements")
    public ResponseEntity<Void> addMovement(@RequestBody AddCashMovementResource resource) {

        commandService.handle(new AddCashMovementCommand(
                resource.type(),
                resource.amount(),
                resource.note()
        ));

        return ResponseEntity.ok().build();
    }

    // Obtener caja actual
    @GetMapping("/current")
    public ResponseEntity<CashRegisterResource> getCurrentCashRegister() {

        CashRegister cashRegister =
                queryService.handle(new GetCurrentCashRegisterQuery());

        return ResponseEntity.ok(
                CashRegisterResourceAssembler.toResource(cashRegister)
        );
    }

    // Obtener caja por id
    @GetMapping("/{id}")
    public ResponseEntity<CashRegisterResource> getCashRegisterById(@PathVariable Long id) {

        CashRegister cashRegister =
                queryService.handle(new GetCashRegisterByIdQuery(id));

        return ResponseEntity.ok(
                CashRegisterResourceAssembler.toResource(cashRegister)
        );
    }

    // Movimientos filtrados por tipo
    @GetMapping("/{id}/movements")
    public ResponseEntity<List<CashMovementResource>> getMovementsByType(@PathVariable Long id,
                                                                         @RequestParam(required = false) String type) {
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

    @GetMapping
    public ResponseEntity<List<CashRegisterResource>> getCashRegisters(
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate) {

        List<CashRegister> cashRegisters =
                queryService.handle(new GetCashRegistersByDateRangeQuery(startDate, endDate));

        return ResponseEntity.ok(
                cashRegisters.stream()
                        .map(CashRegisterResourceAssembler::toResource)
                        .toList()
        );
    }
}