package com.biblioteca.prestamos.exception;

/** Se lanza cuando la solicitud es incorrecta, por ejemplo un parámetro vacío (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
