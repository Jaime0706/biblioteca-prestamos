package com.biblioteca.prestamos.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato único de respuesta para todos los errores de la API.
 * "errors" contiene el detalle por campo cuando hay errores de validación; en otros casos va vacío.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errors,
        String path) {
}
