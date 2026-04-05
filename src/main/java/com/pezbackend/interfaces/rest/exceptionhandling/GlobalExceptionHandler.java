package com.pezbackend.interfaces.rest.exceptionhandling;

import com.pezbackend.shared.domain.model.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.pezbackend.shared.interfaces.rest.resources.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                request.getRequestURI(),
                ex.getMessage(),
                404,
                LocalDateTime.now()
        );

        return ResponseEntity.status(404).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                request.getRequestURI(),
                ex.getMessage(),
                400,
                LocalDateTime.now()
        );

        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                request.getRequestURI(),
                ex.getMessage(),
                422,
                LocalDateTime.now()
        );

        return ResponseEntity.status(422).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                request.getRequestURI(),
                ex.getMessage(),
                401,
                LocalDateTime.now()
        );

        return ResponseEntity.status(401).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                request.getRequestURI(),
                ex.getMessage(),
                403,
                LocalDateTime.now()
        );

        return ResponseEntity.status(403).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                request.getRequestURI(),
                "Internal server error",
                500,
                LocalDateTime.now()
        );

        return ResponseEntity.status(500).body(error);
    }
}