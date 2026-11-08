package com.biblioteca.prestamos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos que el cliente envía para registrar o modificar un libro. */
public record LibroRequestDto(

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 150, message = "El título no puede exceder 150 caracteres.")
        String titulo,

        @NotBlank(message = "El autor es obligatorio.")
        @Size(max = 100, message = "El autor no puede exceder 100 caracteres.")
        String autor,

        @NotBlank(message = "El ISBN es obligatorio.")
        @Pattern(regexp = "^[0-9Xx-]{10,20}$",
                message = "El ISBN solo puede contener dígitos, guiones y X (entre 10 y 20 caracteres).")
        String isbn) {
}
