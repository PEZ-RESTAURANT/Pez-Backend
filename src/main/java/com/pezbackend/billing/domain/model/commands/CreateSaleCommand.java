package com.pezbackend.billing.domain.model.commands;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;
import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

/**
 * Comando para emitir un documento de venta.
 *
 * @param orderId                ID de la comanda
 * @param documentType           tipo de documento (Boleta/Factura)
 * @param customerDocumentNumber número de documento fiscal
 * @param customerName           nombre del cliente (opcional)
 */
public record CreateSaleCommand(
        Long orderId,
        DocumentType documentType,
        String customerDocumentNumber,
        String customerName
) {
    public CreateSaleCommand {
        if (orderId == null || orderId <= 0) {
            throw new BadRequestException("OrderId is required");
        }
        if (documentType == null) {
            throw new BadRequestException("Document type is required");
        }
        if (customerDocumentNumber == null || customerDocumentNumber.isBlank()) {
            throw new BadRequestException("Customer document number is required");
        }
    }
}