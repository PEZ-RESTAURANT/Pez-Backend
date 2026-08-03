package com.pezbackend.shared.domain.exceptions;

/**
 * Excepción lanzada cuando el código de invitación para onboarding es incorrecto o no se proporciona.
 * Se traduce en un código de estado HTTP 403 (Forbidden).
 */
public class InvalidInviteCodeException extends PermissionDeniedException {
    public InvalidInviteCodeException() {
        super("INVALID_INVITE_CODE", "El código de invitación proporcionado es inválido.");
    }
}
