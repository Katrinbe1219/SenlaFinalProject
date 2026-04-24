package org.example.application.exceptions;

public class DoesNoeExist extends RuntimeException {
    public DoesNoeExist(String message) {
        super(message);
    }
}
