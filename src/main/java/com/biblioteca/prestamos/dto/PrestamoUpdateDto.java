package com.biblioteca.prestamos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos que se pueden modificar de un préstamo activo (el libro no se puede cambiar). */
public record PrestamoUpdateDto(

        @NotBlank(message = "El nombre del lector no puede estar vacío.")
        @Size(max = 100, message = "El nombre del lector no puede exceder 100 caracteres.")
        String nombreLector,

        @NotNull(message = "El plazo del préstamo es obligatorio.")
        @Min(value = 1, message = "El plazo del préstamo debe ser al menos de 1 día.")
        @Max(value = 30, message = "El plazo del préstamo no puede exceder 30 días.")
        Integer diasPlazo) {
}
