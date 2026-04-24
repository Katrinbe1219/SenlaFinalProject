package org.example.application.exceptions;

public class NotCorrectInput extends RuntimeException {
    public NotCorrectInput(String message) {
        super(message);
    }
}
