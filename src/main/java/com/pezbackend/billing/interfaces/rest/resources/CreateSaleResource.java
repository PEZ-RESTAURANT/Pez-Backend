package com.pezbackend.billing.interfaces.rest.resources;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;

/**
 * Recurso DTO para recibir el request de emisión de comprobante de pago.
 *
 * @param orderId                ID de la comanda
 * @param documentType           tipo de documento (Boleta/Factura)
 * @param customerDocumentNumber número de documento fiscal
 * @param customerName           nombre del cliente (opcional)
 */
public record CreateSaleResource(
        Long orderId,
        DocumentType documentType,
        String customerDocumentNumber,
        String customerName
) {}