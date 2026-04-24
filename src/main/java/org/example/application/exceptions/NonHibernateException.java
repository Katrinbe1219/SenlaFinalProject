package org.example.application.exceptions;

public class NonHibernateException extends RuntimeException {
    public NonHibernateException(String message) {
        super(message);
    }
}
