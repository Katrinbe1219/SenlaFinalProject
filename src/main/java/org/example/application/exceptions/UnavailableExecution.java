package org.example.application.exceptions;

public class UnavailableExecution extends RuntimeException {
    public UnavailableExecution(String message) {
        super(message);
    }
}
