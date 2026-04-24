package org.example.application.exceptions;

public class EntityAlreadyExist extends RuntimeException {
    public EntityAlreadyExist(String message) {
        super(message);
    }
}
