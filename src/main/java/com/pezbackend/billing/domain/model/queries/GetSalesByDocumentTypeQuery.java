package com.pezbackend.billing.domain.model.queries;

import com.pezbackend.billing.domain.model.valueobjects.DocumentType;

public record GetSalesByDocumentTypeQuery(DocumentType documentType) {
}
