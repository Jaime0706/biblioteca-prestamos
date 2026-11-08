package com.biblioteca.prestamos.dto;

import com.biblioteca.prestamos.entity.EstadoPrestamo;

import java.time.LocalDate;

/** Datos del préstamo que se devuelven al cliente. "vencido" es true si sigue activo y ya pasó la fecha prevista. */
public record PrestamoResponseDto(
        Long id,
        Long libroId,
        String tituloLibro,
        String isbnLibro,
        String nombreLector,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucionPrevista,
        LocalDate fechaDevolucionReal,
        EstadoPrestamo estado,
        boolean vencido) {
}
