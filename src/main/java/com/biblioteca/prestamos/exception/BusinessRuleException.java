package com.biblioteca.prestamos.exception;

/** Se lanza cuando una operación incumple una regla de negocio (HTTP 422). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
