package com.biblioteca.prestamos.dto;

/** Resumen general de la biblioteca. */
public record IndicadoresDto(
        long totalLibros,
        long librosActivos,
        long librosDisponibles,
        long librosPrestados,
        double porcentajeLibrosPrestados,
        long totalPrestamos,
        long prestamosActivos,
        long prestamosDevueltos,
        long prestamosVencidos) {
}
