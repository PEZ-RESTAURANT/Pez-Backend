package com.pezbackend.shared;

import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import com.pezbackend.shared.domain.exceptions.InvalidStateTransitionException;
import com.pezbackend.shared.domain.exceptions.PermissionDeniedException;
import com.pezbackend.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador de prueba utilizado para simular el lanzamiento de excepciones base de negocio
 * y verificar su correcto mapeo a través del {@link com.pezbackend.interfaces.rest.exceptionhandling.GlobalExceptionHandler}.
 */
@RestController
public class TestController {

    @GetMapping("/test/not-found")
    public void throwNotFound() {
        throw new ResourceNotFoundException("TEST_NOT_FOUND", "Resource test not found", Map.of("key", "val"));
    }

    @GetMapping("/test/business-violation")
    public void throwBusinessViolation() {
        throw new BusinessRuleViolationException("TEST_BUSINESS_VIOLATION", "Business rule violated");
    }

    @GetMapping("/test/permission-denied")
    public void throwPermissionDenied() {
        throw new PermissionDeniedException("TEST_PERMISSION_DENIED", "Access forbidden");
    }

    @GetMapping("/test/state-transition")
    public void throwStateTransition() {
        throw new InvalidStateTransitionException("TEST_STATE_TRANSITION", "Invalid state change");
    }
}
