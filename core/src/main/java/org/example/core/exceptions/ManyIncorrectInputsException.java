package org.example.core.exceptions;

import java.util.List;
import java.util.Map;

public class ManyIncorrectInputsException extends RuntimeException {
    private final List<String> errors;
    public ManyIncorrectInputsException(List<String> errors) {
        super("Registration Error");
        this.errors = errors;
    }
    public List<String> getErrors() {
        return errors;
    }
}
