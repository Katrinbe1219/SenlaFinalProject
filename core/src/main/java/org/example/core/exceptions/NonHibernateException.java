package org.example.core.exceptions;

public class NonHibernateException extends RuntimeException {
    public NonHibernateException(String message) {
        super(message);
    }
}
