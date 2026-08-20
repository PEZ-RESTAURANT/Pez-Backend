package com.pezbackend.shared.interfaces.rest.resources;

import java.util.Map;

public record CreateAuditEventResource(
    String eventType,
    String module,
    String deviceId,
    Map<String, Object> payload,
    String reason
) {}
