package org.example.core.exceptions;

import java.util.Map;

public class RegistrationException extends RuntimeException {
    private final Map<String, String> errors;

    public RegistrationException(Map<String, String> errors) {
        super("Registration Error");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
