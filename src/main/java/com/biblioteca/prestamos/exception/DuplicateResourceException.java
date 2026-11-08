package com.biblioteca.prestamos.exception;

/** Se lanza cuando se intenta registrar un dato único que ya existe, por ejemplo un ISBN (HTTP 409). */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
