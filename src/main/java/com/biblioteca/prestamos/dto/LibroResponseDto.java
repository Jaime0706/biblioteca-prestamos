package com.biblioteca.prestamos.dto;

/**
 * Datos del libro que se devuelven al cliente.
 * "activo" indica si el libro está habilitado en el catálogo; "disponible" si además no está prestado.
 */
public record LibroResponseDto(
        Long id,
        String titulo,
        String autor,
        String isbn,
        boolean activo,
        boolean disponible) {
}
