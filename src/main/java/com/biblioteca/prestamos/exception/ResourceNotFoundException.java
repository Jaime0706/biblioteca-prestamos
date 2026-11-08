package com.biblioteca.prestamos.exception;

/** Se lanza cuando un libro o préstamo no existe (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
