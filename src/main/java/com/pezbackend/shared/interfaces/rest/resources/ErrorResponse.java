package com.pezbackend.shared.interfaces.rest.resources;

import java.time.LocalDateTime;

public record ErrorResponse(
        String path,
        String message,
        int status,
        LocalDateTime timestamp
) {}