package org.example.core.models.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.example.core.exceptions.NotCorrectInput;

public enum RoleTypes {
    MIN_USER, MAX_USER, ADMIN, MODERATOR, ANALYST;

    public static RoleTypes getFromString(String value){
        try{
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new NotCorrectInput("Unknown include value: " + value);
        }
    }

    @JsonCreator
    public static RoleTypes fromString(String value){
        return getFromString(value);
    }
}
