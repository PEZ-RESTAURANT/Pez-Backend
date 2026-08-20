package com.pezbackend.billing.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record VoidSaleResource(
        @NotBlank String voidedReason
) {}
